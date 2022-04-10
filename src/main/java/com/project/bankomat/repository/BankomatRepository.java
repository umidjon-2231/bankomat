package com.project.bankomat.repository;

import com.project.bankomat.entity.Bankomat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankomatRepository extends JpaRepository<Bankomat, Long> {
    List<Bankomat> findAllByActiveTrue();
}