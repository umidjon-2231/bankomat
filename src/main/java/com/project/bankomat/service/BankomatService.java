package com.project.bankomat.service;

import com.project.bankomat.dto.ApiResponse;
import com.project.bankomat.dto.BankomatDto;
import com.project.bankomat.dto.BankomatEditDto;
import com.project.bankomat.entity.Bankomat;
import com.project.bankomat.entity.MoneyCount;
import com.project.bankomat.entity.Money;
import com.project.bankomat.entity.User;
import com.project.bankomat.repository.MoneyCountRepository;
import com.project.bankomat.repository.BankomatRepository;
import com.project.bankomat.repository.MoneyRepository;
import com.project.bankomat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BankomatService {
    final BankomatRepository bankomatRepository;
    final UserRepository userRepository;
    final MoneyRepository moneyRepository;
    final MoneyCountRepository moneyCountRepository;

    public ApiResponse add(BankomatDto bankomatDto) {
        Optional<User> optionalUser = userRepository.findById(bankomatDto.getResponsibleUser());
        if(optionalUser.isEmpty()){
            return ApiResponse.builder()
                    .message("Responsible user not found")
                    .build();
        }
        double total=0;
        List<MoneyCount> moneyCounts=new ArrayList<>();
        for (String money : bankomatDto.getCash().keySet()) {
            Optional<Money> optionalMoney = moneyRepository.findBySerialNameIgnoreCase(money);
            if(optionalMoney.isEmpty()){
                return ApiResponse.builder()
                        .message("Money with name \""+money+"\" not found")
                        .build();
            }
            total=Double.sum(total, optionalMoney.get().getAmount().doubleValue()*bankomatDto.getCash().get(money));
            moneyCounts.add(MoneyCount.builder()
                    .money(optionalMoney.get())
                    .count(bankomatDto.getCash().get(money))
                    .build());
        }
        bankomatRepository.save(Bankomat.builder()
                .responsible(optionalUser.get())
                .address(bankomatDto.getAddress())
                .maxTranValue(bankomatDto.getMaxTranValue())
                .commissionForBankCard(bankomatDto.getCommissionForBankCard())
                .commissionOtherCard(bankomatDto.getCommissionOtherCard())
                .minNotificationValue(bankomatDto.getMinNotificationValue())
                .moneyCounts(moneyCounts)
                .amount(BigDecimal.valueOf(total))
                .build());
        return ApiResponse.builder()
                .success(true)
                .message("Bankomat created!")
                .build();
    }

    public ApiResponse edit(Long id, BankomatEditDto bankomatEditDto) {
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        if(optionalBankomat.isEmpty()){
            return ApiResponse.builder()
                    .message("Bankomat not found")
                    .build();
        }
        Bankomat bankomat = optionalBankomat.get();
        if(bankomatEditDto.getAddress()!=null){
            bankomat.setAddress(bankomatEditDto.getAddress());
        }
        if(bankomatEditDto.getCommissionForBankCard()!=null){
            bankomat.setCommissionForBankCard(bankomatEditDto.getCommissionForBankCard());
        }
        if(bankomatEditDto.getCommissionOtherCard()!=null){
            bankomat.setCommissionOtherCard(bankomatEditDto.getCommissionOtherCard());
        }
        if(bankomatEditDto.getMaxTranValue()!=null){
            bankomat.setMaxTranValue(bankomatEditDto.getMaxTranValue());
        }
        if(bankomatEditDto.getResponsibleUser()!=null){
            Optional<User> optionalUser = userRepository.findById(bankomatEditDto.getResponsibleUser());
            if(optionalUser.isEmpty()){
                return ApiResponse.builder()
                        .message("Responsible user not found")
                        .build();
            }
            bankomat.setResponsible(optionalUser.get());
        }
        if(bankomatEditDto.getMinNotificationValue()!=null){
            bankomat.setMinNotificationValue(bankomatEditDto.getMinNotificationValue());
        }
        bankomatRepository.save(bankomat);
        return ApiResponse.builder()
                .message("Bankomat created")
                .success(true)
                .build();
    }


    public ApiResponse delete(Long id){
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        if (optionalBankomat.isEmpty() || !optionalBankomat.get().isActive()) {
            return ApiResponse.builder()
                    .message("Bankomat not found")
                    .build();
        }
        optionalBankomat.get().setActive(false);
        bankomatRepository.save(optionalBankomat.get());

        return ApiResponse.builder()
                .success(true)
                .message("Bankomat deleted!")
                .build();
    }
}
