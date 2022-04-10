package com.project.bankomat.service;

import com.project.bankomat.dto.ApiResponse;
import com.project.bankomat.dto.MoneyDto;
import com.project.bankomat.entity.Money;
import com.project.bankomat.repository.MoneyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MoneyService {
    final MoneyRepository moneyRepository;

    public ApiResponse add(MoneyDto moneyDto) {
        String serial_number=moneyDto.getName().toLowerCase(Locale.ROOT)
                .replaceAll("\\s", "_");
        Optional<Money> bySerialNameIgnoreCase = moneyRepository.findBySerialNameIgnoreCase(serial_number);
        if(bySerialNameIgnoreCase.isPresent()){
            return ApiResponse.builder().message("Not valid name").build();
        }
        Money save = moneyRepository.save(Money.builder()
                .name(moneyDto.getName())
                .serialName(serial_number)
                .amount(BigDecimal.valueOf(moneyDto.getAmount()))
                .build());
        return ApiResponse.builder()
                .message("Money created")
                .success(true)
                .obj(save)
                .build();
    }

    public ApiResponse delete(Long id){
        Optional<Money> optionalMoney = moneyRepository.findById(id);
        if(optionalMoney.isEmpty()){
            return ApiResponse.builder().message("Money not found").build();
        }
        optionalMoney.get().setActive(false);
        return ApiResponse.builder()
                .success(true)
                .message("Money deleted")
                .build();
    }


}
