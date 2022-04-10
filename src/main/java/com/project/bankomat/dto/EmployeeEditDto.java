package com.project.bankomat.dto;

import com.project.bankomat.entity.enums.RoleEnum;
import lombok.Data;

import javax.validation.constraints.Email;
import java.util.List;

@Data
public class EmployeeEditDto {
    private String password;
    @Email
    private String email;
    private String username;
    private Long bank;
}
