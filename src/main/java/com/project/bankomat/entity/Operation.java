package com.project.bankomat.entity;

import com.project.bankomat.entity.Money;
import com.project.bankomat.entity.enums.OperationType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Bankomat bankomat;

    @Builder.Default
    private LocalDateTime time=LocalDateTime.now();

    @Column(nullable = false, precision = 30)
    private BigDecimal amount;
    @ManyToOne(fetch = FetchType.EAGER)
    private Card card;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @ManyToMany
    private List<MoneyCount> moneyCounts;
}
