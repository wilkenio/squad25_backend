package com.financeiro.api.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.financeiro.api.domain.enums.Status;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String accountName;

    private String accountDescription;

    private String additionalInformation;

    //saldo inicial
    private Double openingBalance;

    //saldo atual
    private Double currentBalance;

    //saldo previsto
    private Double expectedBalance;

    private Double specialCheck;

    //variável para armazenar as receitas
    private Double income;

    //variável para armazenar as despesas
    private Double expense;

    //variável para armazenar as receitas previstas
    private Double expectedIncomeMonth;

    //variável para armazenar as despesas previstas
    private Double expectedExpenseMonth;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", unique = false)
    private Category category;
}
