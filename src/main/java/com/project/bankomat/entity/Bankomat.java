package com.project.bankomat.entity;

import com.project.bankomat.entity.Money;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "bankomat")
public class Bankomat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text", nullable = false)
    private String address;

    @Column(nullable = false)
    private BigDecimal maxTranValue;
    @Column(nullable = false)
    private BigDecimal commissionForBankCard;
    @Column(nullable = false)
    private BigDecimal commissionOtherCard;
    @Column(nullable = false)
    private BigDecimal minNotificationValue;
    @ManyToOne
    @JoinColumn(nullable = false)
    private User responsible;

    @Builder.Default
    private boolean active=true;
    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<MoneyCount> moneyCounts;
}
