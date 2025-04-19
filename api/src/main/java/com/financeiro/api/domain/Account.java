package com.financeiro.api.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.financeiro.api.domain.enums.Status;
import java.util.List;

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

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String accountDescription;

    private String additionalInformation;

    private Double openingBalance;

    private Double specialCheck;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany
    @JoinColumn(name = "account_id")
    private List<Category> categories;
}
