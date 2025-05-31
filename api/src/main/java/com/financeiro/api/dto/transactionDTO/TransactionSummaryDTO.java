package com.financeiro.api.dto.transactionDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.financeiro.api.domain.enums.*;

import com.financeiro.api.domain.enums.TransactionType;
import com.financeiro.api.dto.accountDTO.AccountSummaryDTO;
import com.financeiro.api.dto.categoryDTO.CategorySummaryDTO;
import com.financeiro.api.dto.subcategoryDTO.SubcategorySummaryDTO;

public record TransactionSummaryDTO(
    UUID id,
    String name,
    TransactionType type,
    Status status,
    LocalDateTime releaseDate,
    Double value,
    String description,
    TransactionState state,
    String additionalInformation,
    Frequency frequency,
    Integer installments,
    Periodicity periodicity,
    Boolean businessDayOnly,
    Integer installmentNumber,
    UUID recurringGroupId,
    List<AccountSummaryDTO> accounts,
    List<CategorySummaryDTO> categories,
    List<SubcategorySummaryDTO> subcategories
) {}
