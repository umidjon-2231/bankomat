package com.project.bankomat.service;

import com.project.bankomat.config.EmailConfig;
import com.project.bankomat.dto.ApiResponse;
import com.project.bankomat.dto.EmailDto;
import com.project.bankomat.dto.EmployeeDto;
import com.project.bankomat.dto.EmployeeEditDto;
import com.project.bankomat.entity.Bank;
import com.project.bankomat.entity.Role;
import com.project.bankomat.entity.User;
import com.project.bankomat.repository.BankRepository;
import com.project.bankomat.repository.RoleRepository;
import com.project.bankomat.repository.UserRepository;
import com.project.bankomat.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    final UserRepository userRepository;
    final RoleRepository roleRepository;
    final EmailConfig emailConfig;
    final JwtProvider jwtProvider;
    final PasswordEncoder passwordEncoder;
    final BankRepository bankRepository;


    @Value("${company.domain}")
    String domain;

    @SneakyThrows
    @Transactional()
    public ApiResponse add(EmployeeDto employeeDto) {
        Optional<Role> role = roleRepository.findByValue(employeeDto.getRole());
        if (role.isEmpty()) {
            return ApiResponse.builder()
                    .message("Role not found")
                    .build();
        }
        Optional<Bank> optionalBank = bankRepository.findById(employeeDto.getBank());
        if(optionalBank.isEmpty()){
            return ApiResponse.builder()
                    .message("Bank not found")
                    .build();
        }
        User save = userRepository.save(User.builder()
                .email(employeeDto.getEmail())
                .password(passwordEncoder.encode("1234"))
                .role(role.get())
                .username(employeeDto.getUsername())
                .enabled(false)
                .build());
        String verification_url = domain + "api/auth/verify/email?token=" +
                jwtProvider.generateToken(save.getEmail(), 2592000L);
        boolean sendEmailHtml = emailConfig.sendEmailHtml(EmailDto.builder()
                .message(verifyEmailHtml(employeeDto, verification_url))
                .title("Verify email")
                .to(employeeDto.getEmail())
                .build());
        if (!sendEmailHtml) {
            return ApiResponse.builder()
                    .message("Error with email sending")
                    .build();
        }


        return ApiResponse.builder()
                .success(true)
                .message("Saved!")
                .build();
    }

    public String verifyEmailHtml(@Valid EmployeeDto employeeDto, String url) {
        return "<h1>Hello " + employeeDto.getUsername() + "!</h1><br/>" +
                "Bank give this email for verification employee.<br/>" +
                "If you are not working in this company ignore this message<br/><br/><br/><br/>" +
                "<table style=\"border-collapse:collapse;border-spacing:0;margin-top:17px\"><tbody><tr><td style=\"background-color:#5b50d6;border:1px none #dadada;border-radius:3px;font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;padding:12px 35px;text-align:left;vertical-align:top\" align=\"left\" bgcolor=\"#5B50D6\" valign=\"top\"><a href=\"" +
                url + "\" style=\"background-color:#5b50d6;border:none;border-radius:3px;color:white;display:inline-block;font-size:14px;font-weight:bold;outline:none!important;padding:0px;text-decoration:none\" target=\"_blank\" >Verify</a></td></tr></tbody></table>";
    }

    public ApiResponse verifyEmail(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ApiResponse.builder()
                    .message("User not found")
                    .build();
        }
        User user = optionalUser.get();
        user.setEnabled(true);
        userRepository.save(user);
        return ApiResponse.builder()
                .success(true)
                .message("Verified")
                .build();
    }

    public ApiResponse edit(Long id, EmployeeEditDto employeeEditDto, User auth) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ApiResponse.builder()
                    .message("User not found")
                    .build();
        }
        User user = optionalUser.get();
        if (employeeEditDto.getEmail() != null) {
            user.setEmail(employeeEditDto.getEmail());
        }
        if (employeeEditDto.getPassword() != null && Objects.equals(auth.getId(), id)) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            user.setPassword(bCryptPasswordEncoder.encode(employeeEditDto.getPassword()));
        }
        if (employeeEditDto.getUsername() != null) {
            user.setUsername(employeeEditDto.getUsername());
        }
        if (employeeEditDto.getBank() != null) {
            Optional<Bank> optionalBank = bankRepository.findById(employeeEditDto.getBank());
            if(optionalBank.isEmpty()){
                return ApiResponse.builder().message("Bank not found").build();
            }
            user.setBank(optionalBank.get());
        }
        userRepository.save(user);

        return ApiResponse.builder()
                .success(true)
                .message("Edited!")
                .build();
    }

    public ApiResponse delete(Long id) {
        if(!userRepository.existsById(id)){
            return ApiResponse.builder()
                    .message("User not found")
                    .build();
        }
        userRepository.deleteById(id);
        return ApiResponse.builder()
                .success(true)
                .message("Deleted!")
                .build();
    }
}
