package com.financeiro.api.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.financeiro.api.domain.enums.*;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime releaseDate;

    private Double value;

    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionState state;

    private String additionalInformation;

    @Enumerated(EnumType.STRING)
    private Frequency frequency;

    private Integer installments; 

    @Enumerated(EnumType.STRING)
    private Periodicity periodicity; 

    private Boolean businessDayOnly; 

    private Integer installmentNumber; 

    private UUID recurringGroupId; 

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "subcategory_id")
    private Subcategory subcategory;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
}
