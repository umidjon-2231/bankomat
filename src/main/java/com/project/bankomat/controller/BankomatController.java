package com.project.bankomat.controller;

import com.project.bankomat.dto.ApiResponse;
import com.project.bankomat.dto.BankomatDto;
import com.project.bankomat.dto.BankomatEditDto;
import com.project.bankomat.entity.Bankomat;
import com.project.bankomat.repository.BankomatRepository;
import com.project.bankomat.service.BankomatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/bankomat")
@RequiredArgsConstructor
public class BankomatController {
    final BankomatRepository bankomatRepository;
    final BankomatService bankomatService;

    @PreAuthorize("hasAnyAuthority('BANKOMAT_CRUD')")
    @GetMapping("/")
    public HttpEntity<?> getAll(){
        return ResponseEntity.ok().body(bankomatRepository.findAllByActiveTrue());
    }

    @PreAuthorize("hasAnyAuthority('BANKOMAT_CRUD')")
    @GetMapping("/{id}")
    public HttpEntity<?> getOne(@PathVariable Long id){
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        if(optionalBankomat.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(optionalBankomat.get());
    }

    @PreAuthorize("hasAnyAuthority('BANKOMAT_CRUD')")
    @PostMapping()
    public HttpEntity<?> add(@Valid @RequestBody BankomatDto bankomatDto){
        ApiResponse apiResponse=bankomatService.add(bankomatDto);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse);
    }

    @PreAuthorize("hasAnyAuthority('BANKOMAT_CRUD')")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@PathVariable Long id,@Valid @RequestBody BankomatEditDto bankomatEditDto){
        ApiResponse apiResponse=bankomatService.edit(id, bankomatEditDto);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse);
    }

    @PreAuthorize("hasAnyAuthority('BANKOMAT_CRUD')")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable Long id){
        ApiResponse apiResponse = bankomatService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess()?200:400).body(apiResponse);
    }


    @PreAuthorize("hasAnyAuthority('BANKOMAT_CRUD')")
    @GetMapping("/{id}/money")
    public HttpEntity<?> getMoneyCount(@PathVariable Long id){
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        if(optionalBankomat.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(optionalBankomat.get().getMoneyCounts());
    }

}
