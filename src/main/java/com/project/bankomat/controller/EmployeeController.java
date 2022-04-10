package com.project.bankomat.controller;

import com.project.bankomat.dto.ApiResponse;
import com.project.bankomat.dto.EmployeeDto;
import com.project.bankomat.dto.EmployeeEditDto;
import com.project.bankomat.entity.User;
import com.project.bankomat.repository.UserRepository;
import com.project.bankomat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/employee")
public class EmployeeController {
    final UserRepository userRepository;
    final UserService userService;


    @PreAuthorize("hasAnyAuthority('EMPLOYEE_CRUD', 'EMPLOYEE_READ')")
    @GetMapping
    public HttpEntity<?> getAll(){
        List<User> all = userRepository.findAll();
        return ResponseEntity.ok().body(all);
    }
    @PreAuthorize("hasAnyAuthority('EMPLOYEE_CRUD', 'EMPLOYEE_READ')")
    @GetMapping("/{id}")
    public HttpEntity<?> getOne(@PathVariable Long id){
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        User user = optionalUser.get();
        return ResponseEntity.ok().body(user);
    }

    @PreAuthorize("hasAnyAuthority('EMPLOYEE_CRUD', 'EMPLOYEE_ADD')")
    @PostMapping()
    public HttpEntity<?> add(@Valid @RequestBody EmployeeDto employeeDto, @AuthenticationPrincipal User user) {
        ApiResponse apiResponse = userService.add(employeeDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 400).body(apiResponse);
    }

    @PreAuthorize("hasAnyAuthority('EMPLOYEE_CRUD', 'EMPLOYEE_EDIT')")
    @PutMapping("/{id}")
    public HttpEntity<?> edit(@RequestBody EmployeeEditDto employeeEditDto,
                              @PathVariable Long id, @AuthenticationPrincipal User user
    ) {
        ApiResponse apiResponse = userService.edit(id, employeeEditDto, user);
        return ResponseEntity.ok().body(apiResponse);
    }

    @PreAuthorize("hasAnyAuthority('EMPLOYEE_DELETE')")
    @DeleteMapping("/{id}")
    public HttpEntity<?> delete(@PathVariable Long id, @AuthenticationPrincipal User user){
        if(user.getId().equals(id)){
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("You can't delete yourself")
                    .build());
        }
        ApiResponse apiResponse=userService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess()?200:404).body(apiResponse);
    }

}
