package com.project.bankomat.controller;

import com.project.bankomat.dto.ApiResponse;
import com.project.bankomat.dto.MoneyDto;
import com.project.bankomat.repository.MoneyRepository;
import com.project.bankomat.service.MoneyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/money")
@RequiredArgsConstructor
public class MoneyController {
    final MoneyService moneyService;
    final MoneyRepository moneyRepository;

    @GetMapping
    public HttpEntity<?> getAll(){
        return ResponseEntity.ok().body(moneyRepository.findAll());
    }

    @PreAuthorize("hasAnyAuthority('MONEY_CRUD')")
    @PostMapping()
    public HttpEntity<?> add(@RequestBody MoneyDto moneyDto){
        ApiResponse apiResponse=moneyService.add(moneyDto);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse);
    }

    @PreAuthorize("hasAnyAuthority('MONEY_CRUD')")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable Long id){
        ApiResponse apiResponse = moneyService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse.getMessage());
    }

}
