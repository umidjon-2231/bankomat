package com.project.bankomat.service;

import com.project.bankomat.dto.ApiResponse;
import com.project.bankomat.dto.CardDto;
import com.project.bankomat.dto.CardLoginDto;
import com.project.bankomat.entity.Bank;
import com.project.bankomat.entity.Card;
import com.project.bankomat.entity.User;
import com.project.bankomat.repository.CardRepository;
import com.project.bankomat.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {
    final CardRepository cardRepository;
    final PasswordEncoder passwordEncoder;
    final JwtProvider jwtProvider;

    public Long generate(Long min, Long max){
        return Math.round(Math.random()*(max-min)+min);
    }

    public Long generateNumber(){
        Long number=Long.parseLong("8600"+generate(1000L, 9999L)+generate(1000L, 9999L)+generate(1000L, 9999L));
        if (cardRepository.existsById(number)) {
            return generateNumber();
        }
        return number;
    }

    public ApiResponse add(CardDto cardDto, User auth) {
        Bank bank = auth.getBank();
        Card save = cardRepository.save(Card.builder()
                .first_name(cardDto.getFirst_name())
                .number(generateNumber())
                .last_name(cardDto.getLast_name())
                .cvv(Short.valueOf("" + Math.round(Math.random() * (999 - 100) + 100)))
                .due(LocalDate.now().plusYears(5))
                .password(passwordEncoder.encode(String.valueOf(cardDto.getPassword())))
                .type(bank.getCard())
                .amount(BigDecimal.valueOf(cardDto.getAmount()))
                .build());
        return ApiResponse.builder()
                .success(true)
                .obj(save)
                .message("Card created!")
                .build();
    }

    public ApiResponse block(Long number) {
        Optional<Card> optionalCard = cardRepository.findById(number);
        if(optionalCard.isEmpty()){
            return ApiResponse.builder()
                    .message("Not found")
                    .build();
        }
        if(!optionalCard.get().isActive()){
            return ApiResponse.builder()
                    .message("Card already blocked")
                    .build();
        }
        optionalCard.get().setActive(false);
        cardRepository.save(optionalCard.get());
        return ApiResponse.builder()
                .success(true)
                .message("Success blocked!")
                .build();
    }

    public ApiResponse unblock(Long number) {
        Optional<Card> optionalCard = cardRepository.findById(number);
        if(optionalCard.isEmpty()){
            return ApiResponse.builder()
                    .message("Not found")
                    .build();
        }
        if(optionalCard.get().isActive()){
            return ApiResponse.builder()
                    .message("Card already active!")
                    .build();
        }
        optionalCard.get().setActive(true);
        cardRepository.save(optionalCard.get());
        return ApiResponse.builder()
                .success(true)
                .message("Success unblocked!")
                .build();
    }

    public ApiResponse changePassword(Long number, String password) {
        Optional<Card> optionalCard = cardRepository.findById(number);
        if(optionalCard.isEmpty()){
            return ApiResponse.builder()
                    .message("Not found")
                    .build();
        }
        optionalCard.get().setPassword(passwordEncoder.encode(password));
        return ApiResponse.builder()
                .success(true)
                .message("Password changed")
                .build();
    }


    public ApiResponse dueMultiply(Long number, LocalDate due) {
        Optional<Card> optionalCard = cardRepository.findById(number);
        if(optionalCard.isEmpty()){
            return ApiResponse.builder().message("Card not found").build();
        }
        Card card = optionalCard.get();
        card.setActive(true);
        card.setDue(due);
        cardRepository.save(card);
        return ApiResponse.builder()
                .success(true)
                .message("Card due multiplied")
                .build();
    }
}
