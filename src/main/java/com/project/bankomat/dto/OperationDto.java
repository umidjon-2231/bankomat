package com.project.bankomat.dto;


import com.project.bankomat.entity.Money;
import com.project.bankomat.entity.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationDto {
    private Map<String, Integer> cash;
}
