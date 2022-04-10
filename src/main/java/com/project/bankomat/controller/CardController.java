package com.project.bankomat.controller;

import com.project.bankomat.dto.ApiResponse;
import com.project.bankomat.dto.CardDto;
import com.project.bankomat.dto.CardLoginDto;
import com.project.bankomat.entity.Card;
import com.project.bankomat.entity.User;
import com.project.bankomat.repository.CardRepository;
import com.project.bankomat.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/card")
public class CardController {
    final CardRepository cardRepository;
    final CardService cardService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CARD_CRUD')")
    public HttpEntity<?> add(@Valid @RequestBody CardDto cardDto, @AuthenticationPrincipal User user){
        ApiResponse apiResponse=cardService.add(cardDto, user);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse);
    }

    @PostMapping("/block/{number}")
    @PreAuthorize("hasAnyAuthority('CARD_CRUD')")
    public HttpEntity<?> blockCard(@PathVariable Long number){
        ApiResponse apiResponse=cardService.block(number);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse.getMessage());
    }
    @PostMapping("/unblock/{number}")
    @PreAuthorize("hasAnyAuthority('CARD_CRUD')")
    public HttpEntity<?> unblockCard(@PathVariable Long number){
        ApiResponse apiResponse=cardService.unblock(number);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse.getMessage());
    }


    @PatchMapping("/password/{number}")
    @PreAuthorize("hasAnyAuthority('CARD_CRUD')")
    public HttpEntity<?> changePassword(@PathVariable Long number, @Size(min = 4, max = 4) @RequestBody String password){
        ApiResponse apiResponse=cardService.changePassword(number, password);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse.getMessage());
    }

    @PreAuthorize("hasAnyAuthority('CARD_CRUD')")
    @PostMapping("/due/{number}")
    public HttpEntity<?> dueMultiply(@PathVariable Long number, @RequestBody Map<String, Object> response){
        ApiResponse apiResponse=cardService.dueMultiply(number, LocalDate.parse(response.get("due").toString(), DateTimeFormatter.ISO_DATE));
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse);
    }



}
