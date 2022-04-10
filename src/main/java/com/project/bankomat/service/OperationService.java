package com.project.bankomat.service;

import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.Gson;
import com.project.bankomat.config.EmailConfig;
import com.project.bankomat.dto.*;
import com.project.bankomat.entity.*;
import com.project.bankomat.entity.enums.OperationType;
import com.project.bankomat.repository.*;
import com.project.bankomat.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperationService {
    final BankomatRepository bankomatRepository;
    final OperationRepository operationRepository;
    final MoneyRepository moneyRepository;
    final MoneyCountRepository moneyCountRepository;
    final CardRepository cardRepository;
    final PasswordEncoder passwordEncoder;
    final JwtProvider jwtProvider;
    final Gson gson;
    final EmailConfig emailConfig;
    Map<LocalDateTime, Long> tries=new LinkedHashMap<>();

    @Value("${company.domain}")
    String domain;

    public ApiResponse input(Long id, OperationDto operationDto, HttpServletRequest request) {
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        if(optionalBankomat.isEmpty()){
            return ApiResponse.builder()
                    .message("Bankomat not found")
                    .build();
        }
        Bankomat bankomat = optionalBankomat.get();
        String authorization = request.getHeader("Authorization");
        if(authorization==null || authorization.split(" ").length<2){
            return ApiResponse.builder()
                    .message("Auth failed")
                    .build();
        }
        String token = authorization.split(" ")[1];
        if (!jwtProvider.checkToken(token)) {
            return ApiResponse.builder()
                    .message("Token failed")
                    .build();
        }
        CardLoginDto tokenObj = gson.fromJson(jwtProvider.getSubjectFromToken(token), CardLoginDto.class);
        if(!id.equals(tokenObj.getBankomat())){
            return ApiResponse.builder()
                    .message("Wrong token info")
                    .build();
        }
        double total=0;
        Map<MoneyCount, Integer> moneyCounts=new LinkedHashMap<>();
        for (String money : operationDto.getCash().keySet()) {
            Optional<Money> optionalMoney = moneyRepository.findBySerialNameIgnoreCase(money);
            if(optionalMoney.isEmpty()){
                return ApiResponse.builder().message("Money with name \""+money+"\" not found").build();
            }
            List<MoneyCount> filteredMoney= bankomat.getMoneyCounts().stream()
                    .filter(moneyCount -> moneyCount.getMoney().getId().equals(optionalMoney.get().getId())).toList();
            if (filteredMoney.size()==0) {
                return ApiResponse.builder().message("Bankomat have not any cell for money "+money).build();
            }
            moneyCounts.put(filteredMoney.get(0), operationDto.getCash().get(money));
            total+=optionalMoney.get().getAmount().doubleValue()*operationDto.getCash().get(money);
        }
        if(total>=bankomat.getMaxTranValue().doubleValue()){
            return ApiResponse.builder()
                    .message("Max transaction value is "+bankomat.getMaxTranValue())
                    .build();
        }
        ApiResponse cardStatus = checkCard(tokenObj.getNumber());
        if(!cardStatus.isSuccess()){
            return ApiResponse.builder().message(cardStatus.getMessage()).build();
        }
        Card card =(Card) cardStatus.getObj();
        card.setAmount(card.getAmount().add(BigDecimal.valueOf(total-commission(bankomat, card, total))));
        cardRepository.save(card);
        for (MoneyCount moneyCount : moneyCounts.keySet()) {
            moneyCount.setCount(moneyCount.getCount()+moneyCounts.get(moneyCount));
            moneyCountRepository.save(moneyCount);
        }
        bankomat.setAmount(bankomat.getAmount().add(BigDecimal.valueOf(total)));
        bankomatRepository.save(bankomat);
        Operation save = operationRepository.save(Operation.builder()
                .amount(BigDecimal.valueOf(total))
                .bankomat(bankomat)
                .card(card)
                .moneyCounts(moneyCounts.keySet().stream().toList())
                .operationType(OperationType.INPUT)
                .build());


        return ApiResponse.builder()
                .success(true)
                .message("Operation success!")
                .obj(save)
                .build();
    }

    public ApiResponse output(Long id, OperationOutputDto operationDto, HttpServletRequest request) {
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        if(optionalBankomat.isEmpty()){
            return ApiResponse.builder()
                    .message("Bankomat not found")
                    .build();
        }
        Bankomat bankomat = optionalBankomat.get();
        String authorization = request.getHeader("Authorization");
        if(authorization==null || authorization.split(" ").length<2){
            return ApiResponse.builder()
                    .message("Auth failed")
                    .build();
        }
        String token = authorization.split(" ")[1];
        if (!jwtProvider.checkToken(token)) {
            return ApiResponse.builder()
                    .message("Token failed")
                    .build();
        }
        CardLoginDto tokenObj = gson.fromJson(jwtProvider.getSubjectFromToken(token), CardLoginDto.class);
        if(!id.equals(tokenObj.getBankomat())){
            return ApiResponse.builder()
                    .message("Wrong token info")
                    .build();
        }
        if(operationDto.getAmount()>=bankomat.getMaxTranValue().doubleValue()){
            return ApiResponse.builder()
                    .message("Max transaction value is "+bankomat.getMaxTranValue())
                    .build();
        }
        if(operationDto.getAmount()>bankomat.getAmount().doubleValue()){
            return ApiResponse.builder()
                    .message("Bankomatda buncha pul yoq")
                    .build();
        }
        ApiResponse cardStatus = checkCard(tokenObj.getNumber());
        if(!cardStatus.isSuccess()){
            return ApiResponse.builder().message(cardStatus.getMessage()).build();
        }
        Card card =(Card) cardStatus.getObj();
        if(card.getAmount().doubleValue()<=operationDto.getAmount()){
            return ApiResponse.builder()
                    .message("Amount greater than card amount")
                    .build();
        }
        Map<MoneyCount, Integer> moneyCounts=new LinkedHashMap<>();
        bankomat.getMoneyCounts().sort((o1, o2) ->
                o1.getMoney().getAmount().doubleValue()>o2.getMoney().getAmount().doubleValue()?0:1);
        double left=operationDto.getAmount();
        for (MoneyCount moneyCount : bankomat.getMoneyCounts()) {
            if(left==0)break;
            double value = moneyCount.getMoney().getAmount().doubleValue();
            if(value>left){
                continue;
            }
            int count;
            if (value==left){
                if(moneyCount.getCount()==0){
                    continue;
                }
                count=1;
            }else {
                if(left/value<=moneyCount.getCount()){
                    count=(int)(left/value);
                }else {
                    continue;
                }
            }
            moneyCount.setCount(moneyCount.getCount()-count);
            moneyCounts.put(moneyCount, count);
            left-=moneyCount.getMoney().getAmount().multiply(BigDecimal.valueOf(count)).doubleValue();
        }
        if(left!=0){
            return ApiResponse.builder().message("Bankomatda kerakli pullar yoq").build();
        }
        card.setAmount(BigDecimal.valueOf(card.getAmount().doubleValue()-operationDto.getAmount()
                -commission(bankomat, card, operationDto.getAmount())));
        cardRepository.save(card);
        bankomat.setAmount(BigDecimal.valueOf(bankomat.getAmount().doubleValue()-operationDto.getAmount()));
        bankomatRepository.save(bankomat);
        if (bankomat.getAmount().doubleValue()<=bankomat.getMinNotificationValue().doubleValue()) {
            Thread thread=new Thread(() -> sendBankomatAmountMinNotification(bankomat));
            thread.start();
        }
        Map<String, Integer> moneyCountDto=new LinkedHashMap<>();
        for (MoneyCount moneyCount : moneyCounts.keySet()) {
            moneyCountDto.put(moneyCount.getMoney().getSerialName(), moneyCounts.get(moneyCount));
        }
        operationRepository.save(Operation.builder()
                .amount(BigDecimal.valueOf(operationDto.getAmount()))
                .bankomat(bankomat)
                .card(card)
                .moneyCounts(moneyCounts.keySet().stream().toList())
                .operationType(OperationType.OUTPUT)
                .build());

        return ApiResponse.builder()
                .success(true)
                .message("Operation success!")
                .obj(moneyCountDto)
                .build();
    }

    public ApiResponse check(CardLoginDto cardLoginDto) {
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(cardLoginDto.getBankomat());
        if(optionalBankomat.isEmpty()){
            return ApiResponse.builder()
                    .message("Bankomat not found")
                    .build();
        }
        ApiResponse apiResponse = checkCard(cardLoginDto.getNumber());
        if (!apiResponse.isSuccess()) {
            return ApiResponse.builder().message(apiResponse.getMessage()).build();
        }
        Card card = (Card)apiResponse.getObj();
        if (!passwordEncoder.matches(cardLoginDto.getPassword(), card.getPassword())) {
            int count=0;
            for (LocalDateTime localDateTime : tries.keySet().stream().filter(localDateTime ->
                    tries.get(localDateTime).equals(card.getNumber())).toList()) {
                if(localDateTime.isBefore(LocalDateTime.now().minusHours(1))){
                    continue;
                }
                count++;
            }
            if(count>=2){
                card.setActive(false);
                cardRepository.save(card);
                tries.values().remove(card.getNumber());
                return ApiResponse.builder()
                        .message("Card blocked")
                        .build();
            }
            tries.put(LocalDateTime.now(), card.getNumber());
            return ApiResponse.builder()
                    .message("Invalid password")
                    .build();
        }
        String token = jwtProvider.generateToken(gson.toJson(cardLoginDto), 1800000L);
        return ApiResponse.builder()
                .success(true)
                .message("Success")
                .obj(token)
                .build();
    }

    public ApiResponse replenish(Long id, OperationDto operationDto, User user) {
        Optional<Bankomat> bankomatOptional = bankomatRepository.findById(id);
        if(bankomatOptional.isEmpty()){
            return ApiResponse.builder().message("Bankomat not found").build();
        }
        Bankomat bankomat = bankomatOptional.get();
        if(!bankomat.getResponsible().getId().equals(user.getId())){
            return ApiResponse.builder().message("Bankomatga mas'ul odam emassiz!").build();
        }
        Map<MoneyCount, Integer> moneyCounts=new LinkedHashMap<>();
        double total=0;
        for (String money : operationDto.getCash().keySet()) {
            Optional<Money> optionalMoney = moneyRepository.findBySerialNameIgnoreCase(money);
            if(optionalMoney.isEmpty()){
                return ApiResponse.builder().message("Money with name \""+money+"\" not found").build();
            }
            List<MoneyCount> filteredMoney= bankomat.getMoneyCounts().stream()
                    .filter(moneyCount -> moneyCount.getMoney().getId().equals(optionalMoney.get().getId())).toList();
            if (filteredMoney.size()==0) {
                return ApiResponse.builder().message("Bankomat have not any cell for money "+money).build();
            }
            MoneyCount moneyCount = filteredMoney.get(0);
            moneyCounts.put(moneyCount, operationDto.getCash().get(money));
            total+=optionalMoney.get().getAmount().doubleValue()*operationDto.getCash().get(money);
        }
        for (MoneyCount moneyCount : moneyCounts.keySet()) {
            moneyCount.setCount(moneyCount.getCount()+moneyCounts.get(moneyCount));
            moneyCountRepository.save(moneyCount);
        }
        bankomat.setAmount(bankomat.getAmount().add(BigDecimal.valueOf(total)));
        bankomatRepository.save(bankomat);
        Operation save = operationRepository.save(Operation.builder()
                .amount(BigDecimal.valueOf(total))
                .bankomat(bankomat)
                .moneyCounts(moneyCounts.keySet().stream().toList())
                .operationType(OperationType.REPLENISH)
                .build());

        return ApiResponse.builder()
                .success(true)
                .obj(save)
                .message("Success replenish!")
                .build();
    }
    
    public void sendBankomatAmountMinNotification(Bankomat bankomat){
        User responsible = bankomat.getResponsible();
        StringBuilder moneyList=new StringBuilder();
        for (MoneyCount moneyCount : bankomat.getMoneyCounts()) {
            moneyList.append("<b>").append(moneyCount.getMoney().getName())
                    .append(" - ").append(moneyCount.getCount())
                    .append(" = ").append(moneyCount.getCount()*moneyCount.getMoney().getAmount().doubleValue())
                    .append("<br/>");
        }
        emailConfig.sendEmailHtml(EmailDto.builder()
                        .to(responsible.getEmail())
                        .title("Bankomatda pul tugamoqda")
                        .message("<h1> Assalomu aleykum " +responsible.getUsername()+ "!</h1><br/><br/>" +
                                "<b>" + bankomat.getAddress()+"</b> addresida joylashgan bankomatda <b>"
                                + bankomat.getAmount()+"<b/> miqdorda pul qoldi<br/><br/>" +
                                moneyList+"<br/>" +
                                "<a href=\""+domain+"api/bankomat/" + bankomat.getId()+"\">Batafsil</a>")

                .build());
    }

    public ApiResponse checkCard(Long id){
        Optional<Card> optionalCard = cardRepository.findById(id);
        if(optionalCard.isEmpty()){
            return ApiResponse.builder().message("Card not found").build();
        }
        Card card = optionalCard.get();
        if(!card.isActive()){
            return ApiResponse.builder().message("Card blocked").build();
        }
        if(card.getDue().isBefore(LocalDate.now())){
            return ApiResponse.builder().message("Card expired").build();
        }
        return ApiResponse.builder().success(true).obj(optionalCard.get()).build();
    }


    private double commission(Bankomat bankomat, Card card, double amount){
        return amount/100*(bankomat.getResponsible().getBank().getCard()==card.getType()
                ?bankomat.getCommissionForBankCard().doubleValue():bankomat.getCommissionOtherCard().doubleValue());
    }
}