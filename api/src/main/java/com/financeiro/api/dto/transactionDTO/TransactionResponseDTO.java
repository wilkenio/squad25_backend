package com.financeiro.api.dto.transactionDTO;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Subcategory;
import com.financeiro.api.domain.enums.*;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponseDTO(
                String accountName,
                String categoryName,
                String categoryIconClass,
                String categoryColor,
                String subcategoryName,
                String subcategoryIconClass,
                String subcategoryColor,
                String name,
                TransactionType type,
                Status status,
                LocalDateTime releaseDate,
                Double value,
                String description,
                TransactionState state,
                String additionalInformation,
                Frequency frequency,
                Integer installments
) {
}
