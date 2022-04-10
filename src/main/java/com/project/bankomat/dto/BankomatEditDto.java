package com.project.bankomat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankomatEditDto {
    @NotBlank
    private String address;
    @NotNull
    private Long responsibleUser;
    @NotNull
    private BigDecimal maxTranValue, commissionForBankCard,
            commissionOtherCard, minNotificationValue;
}
