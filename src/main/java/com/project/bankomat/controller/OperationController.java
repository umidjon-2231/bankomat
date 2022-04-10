package com.project.bankomat.controller;

import com.project.bankomat.config.EmailConfig;
import com.project.bankomat.dto.ApiResponse;
import com.project.bankomat.dto.CardLoginDto;
import com.project.bankomat.dto.OperationDto;
import com.project.bankomat.dto.OperationOutputDto;
import com.project.bankomat.entity.Operation;
import com.project.bankomat.entity.User;
import com.project.bankomat.entity.enums.OperationType;
import com.project.bankomat.repository.BankomatRepository;
import com.project.bankomat.repository.OperationRepository;
import com.project.bankomat.service.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/operation")
@RequiredArgsConstructor
public class OperationController {
    final BankomatRepository bankomatRepository;
    final OperationService operationService;
    final OperationRepository operationRepository;


    @PostMapping("/input/{id}")
    public HttpEntity<?> input(@PathVariable Long id, @RequestBody OperationDto operationDto, HttpServletRequest request){
        ApiResponse apiResponse=operationService.input(id, operationDto, request);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse);
    }

    @PostMapping("/output/{id}")
    public HttpEntity<?> output(@PathVariable Long id, @RequestBody OperationOutputDto operationDto, HttpServletRequest request){
        ApiResponse apiResponse=operationService.output(id, operationDto, request);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse);
    }


    @PostMapping("/replenish/{id}")
    @PreAuthorize("hasAnyAuthority('CARD_CRUD')")
    public HttpEntity<?> replenish(@PathVariable Long id, @RequestBody OperationDto operationDto, @AuthenticationPrincipal User user){
        ApiResponse apiResponse=operationService.replenish(id, operationDto, user);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse);
    }

    @PostMapping("/check")
    public HttpEntity<?> getToken(@Valid @RequestBody CardLoginDto cardLoginDto){
        ApiResponse apiResponse=operationService.check(cardLoginDto);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse);
    }


    @GetMapping("/output/{id}/today")
    public HttpEntity<?> todayOutput(@PathVariable Long id){
        List<Operation> result = operationRepository.findAllByTimeAfterAndOperationTypeAndBankomat_id(LocalDateTime.now().withHour(0),
                OperationType.OUTPUT, id);
        return ResponseEntity.status(bankomatRepository.existsById(id)?200:404).body(result);
    }

    @GetMapping("/input/{id}/today")
    public HttpEntity<?> todayInput(@PathVariable Long id){
        List<Operation> result = operationRepository.findAllByTimeAfterAndOperationTypeAndBankomat_id(LocalDateTime.now().withHour(0),
                OperationType.INPUT, id);
        return ResponseEntity.status(bankomatRepository.existsById(id)?200:404).body(result);
    }

    @PreAuthorize("hasAnyAuthority('REPLENISH_READ')")
    @GetMapping("/replenish/{id}")
    public HttpEntity<?> replenish(@PathVariable Long id){
        List<Operation> result = operationRepository.findAllByOperationTypeAndBankomat_id(OperationType.REPLENISH, id);
        return ResponseEntity.status(bankomatRepository.existsById(id)?200:404).body(result);
    }

}
