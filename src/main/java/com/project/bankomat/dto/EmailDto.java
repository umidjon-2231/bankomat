package com.project.bankomat.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
//@AllArgsConstructor

@RequiredArgsConstructor
public class EmailDto {
    final String to, message, title;
}
