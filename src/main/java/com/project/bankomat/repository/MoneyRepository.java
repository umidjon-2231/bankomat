package com.project.bankomat.repository;

import com.project.bankomat.entity.Money;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MoneyRepository extends JpaRepository<Money, Long> {
    Optional<Money> findByNameIgnoreCase(String name);

    Optional<Money> findBySerialNameIgnoreCase(String money);
}