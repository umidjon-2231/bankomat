package com.project.bankomat.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "money")
public class Money {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @Builder.Default
    private boolean active=true;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String serialName;
    @Column(nullable = false)
    private BigDecimal amount;
}
