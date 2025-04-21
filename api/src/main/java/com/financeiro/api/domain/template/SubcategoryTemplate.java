package com.financeiro.api.domain.template;

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
public class SubcategoryTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    private String iconClass;
    private String color;
    private String additionalInfo;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "category_template_id")
    private CategoryTemplate categoryTemplate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
