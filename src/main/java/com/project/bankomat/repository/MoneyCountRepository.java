package com.project.bankomat.repository;

import com.project.bankomat.entity.MoneyCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoneyCountRepository extends JpaRepository<MoneyCount, Long> {
}