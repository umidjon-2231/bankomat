package com.project.bankomat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardLoginDto {
    @NotNull
    private Long number;
    @NotBlank
    @Size(min = 4, max = 4)
    private String password;

    @NotNull
    private Long bankomat;
}
