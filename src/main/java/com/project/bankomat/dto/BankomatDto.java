package com.project.bankomat.dto;

import com.project.bankomat.entity.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BankomatDto {
    @NotBlank
    private String address;
    @NotNull
    private Long responsibleUser;
    @NotNull
    private BigDecimal maxTranValue, commissionForBankCard, commissionOtherCard, minNotificationValue;
    @NotNull
    @Size(min = 1)
    private Map<String, Integer> cash;
}
