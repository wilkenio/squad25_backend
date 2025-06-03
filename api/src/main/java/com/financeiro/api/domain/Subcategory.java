package com.financeiro.api.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.financeiro.api.domain.enums.*;

@Entity
@Table(name = "subcategory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subcategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name_subcategory", nullable = false)
    private String name;

    @Column(name = "standard_recommendation")
    private Boolean standardRecommendation;

    @Column(name = "icon_class")
    private String iconClass;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "color")
    private String color;

    @Column(name = "additional_info")
    private String additionalInfo;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "update_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "id_category")
    private Category category;
}
