package com.project.bankomat.dto;

import com.project.bankomat.entity.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeDto {
    @Email(message = "Email required field")
    @NotNull
    private String email;
    @NotNull
    private RoleEnum role;
    @NotNull
    @NotBlank
    private String username;
    private Long bank;
}
