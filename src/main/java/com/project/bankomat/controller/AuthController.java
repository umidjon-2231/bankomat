package com.project.bankomat.controller;

import com.project.bankomat.config.EmailConfig;
import com.project.bankomat.dto.ApiResponse;
import com.project.bankomat.dto.LoginDto;
import com.project.bankomat.entity.User;
import com.project.bankomat.repository.UserRepository;
import com.project.bankomat.security.JwtProvider;
import com.project.bankomat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    final UserRepository userRepository;
    final UserService userService;
    final JwtProvider jwtProvider;
    final EmailConfig emailConfig;


    @PostMapping("/login")
    public HttpEntity<?> login(@Valid @RequestBody LoginDto loginDto, HttpServletResponse res) {
        Optional<User> byUsername = userRepository.findByEmail(loginDto.getEmail());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (byUsername.isEmpty() || !byUsername.get().isEnabled() || !passwordEncoder.matches(loginDto.getPassword(), byUsername.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid username or password");
        }
        String token = jwtProvider.generateToken(loginDto.getEmail());
        Cookie cookie = new Cookie("token", token);
        res.addCookie(cookie);
        return ResponseEntity.ok().body(token);
    }

    @SneakyThrows
    @PostMapping("/verify/email")
    public HttpEntity<?> send(@Param("token") String token) {
        if (jwtProvider.checkToken(token)) {
            Optional<User> optionalUser = userRepository.findByEmail(jwtProvider.getSubjectFromToken(token));
            if (optionalUser.isEmpty() || optionalUser.get().isEnabled()) {
                return ResponseEntity.badRequest().body("token failed");
            }
            ApiResponse apiResponse = userService.verifyEmail(optionalUser.get().getId());
            if (apiResponse.isSuccess()) {
                return ResponseEntity.ok()
                        .body(apiResponse);
            }
        }

        return ResponseEntity.badRequest().body("Failed!");
    }


}
