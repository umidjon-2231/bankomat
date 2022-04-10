package com.project.bankomat.repository;

import com.project.bankomat.entity.Operation;
import com.project.bankomat.entity.enums.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findAllByTimeAfterAndOperationTypeAndBankomat_id(LocalDateTime withHour, OperationType output, Long id);

    List<Operation> findAllByOperationTypeAndBankomat_id(OperationType replenish, Long id);
}