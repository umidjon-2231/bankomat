package com.project.bankomat.component;

import com.project.bankomat.entity.Bank;
import com.project.bankomat.entity.Money;
import com.project.bankomat.entity.Role;
import com.project.bankomat.entity.User;
import com.project.bankomat.entity.enums.CardEnum;
import com.project.bankomat.entity.enums.PermissionEnum;
import com.project.bankomat.entity.enums.RoleEnum;
import com.project.bankomat.repository.BankRepository;
import com.project.bankomat.repository.MoneyRepository;
import com.project.bankomat.repository.RoleRepository;
import com.project.bankomat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    final UserRepository userRepository;
    final RoleRepository roleRepository;
    final PasswordEncoder passwordEncoder;
    final BankRepository bankRepository;
    final MoneyRepository moneyRepository;


    @Value("${spring.sql.init.mode}")
    private String initMode;

    @Override
    public void run(String... args) {
        if(initMode.equalsIgnoreCase("always")){
            PermissionEnum[] permissionEnums = PermissionEnum.values();
            Bank nbu = bankRepository.save(Bank.builder()
                    .address("Toshkent shahar, Mirobod tumani, 8-mart ko'chasi")
                    .card(CardEnum.UZCARD)
                    .name("NBU")
                    .build());
            userRepository.save(User.builder()
                            .password(passwordEncoder.encode("1234"))
                            .username("director")
                            .email("director@gmail.com")
                            .bank(nbu)
                            .role(Role.builder()
                                    .value(RoleEnum.DIRECTOR)
                                    .permissions(Arrays.stream(permissionEnums).toList())
                                    .build())
                    .build());
            userRepository.save(User.builder()
                            .role(Role.builder()
                                    .value(RoleEnum.MANAGER)
                                    .permissions(List.of(
                                            PermissionEnum.REPLENISH_ATM,
                                            PermissionEnum.BANKOMAT_CRUD, PermissionEnum.CARD_CRUD,
                                            PermissionEnum.MONEY_CRUD
                                    ))
                                    .build())
                            .bank(nbu)
                            .email("manager@gmail.com")
                            .username("manager")
                            .password(passwordEncoder.encode("1234"))
                    .build());
            moneyRepository.saveAll(List.of(
                    Money.builder().name("One dollar").serialName("ONE_DOLLAR").amount(BigDecimal.ONE).build(),
                    Money.builder().name("Ten dollar").serialName("TEN_DOLLAR").amount(BigDecimal.TEN).build(),
                    Money.builder().name("Fifty dollar").serialName("FIFTY_DOLLAR").amount(BigDecimal.valueOf(50)).build(),
                    Money.builder().name("Hundred dollar").serialName("HUNDRED_DOLLAR").amount(BigDecimal.valueOf(100)).build(),
                    Money.builder().name("Five dollar").serialName("FIVE_DOLLAR").amount(BigDecimal.valueOf(5)).build()
            ));
        }

    }

}
