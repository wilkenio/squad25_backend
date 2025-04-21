package com.financeiro.api.domain.template;

import com.financeiro.api.domain.enums.CategoryType;
import com.financeiro.api.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String iconClass;
    private String color;
    private String additionalInfo;
    private boolean standardRecommendation;

    @Enumerated(EnumType.STRING)
    private CategoryType type;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
