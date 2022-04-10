package com.project.bankomat.entity;

import com.project.bankomat.entity.enums.CardEnum;
import lombok.*;

import javax.persistence.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "bank")
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "text", nullable = false)
    private String address;
    @Enumerated(EnumType.STRING)
    private CardEnum card;
}
