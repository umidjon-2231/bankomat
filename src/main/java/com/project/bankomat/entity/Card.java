package com.project.bankomat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.bankomat.entity.enums.CardEnum;
import lombok.*;
import org.hibernate.annotations.GenerationTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "card")
public class Card {
    @Id
    @Column(length = 16, nullable = false, updatable = false, unique = true)
    private Long number;
    @Column(length = 3, nullable = false)
    private Short cvv;
    @Column(nullable = false)
    private String first_name, last_name;
    @Column(nullable = false)
    private LocalDate due;
    @Builder.Default
    @Column(nullable = false, columnDefinition = "date")
    private LocalDate issued=LocalDate.now();
    @JsonIgnore
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardEnum type;
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal amount=BigDecimal.ZERO;
    @Builder.Default
    @Column(nullable = false)
    private boolean active=true;
}
