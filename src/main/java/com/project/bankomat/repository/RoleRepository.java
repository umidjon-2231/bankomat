package com.project.bankomat.repository;

import com.project.bankomat.entity.Role;
import com.project.bankomat.entity.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByValue(RoleEnum role);
}