package com.financeiro.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.financeiro.api.domain.enums.TransactionOrder;
import com.financeiro.api.dto.accountDTO.AccountSummaryDTO;
import com.financeiro.api.dto.categoryDTO.CategorySummaryDTO;
import com.financeiro.api.dto.subcategoryDTO.SubcategorySummaryDTO;
import com.financeiro.api.dto.transactionDTO.TransactionSummaryDTO;

public record SummaryDTO(
        LocalDateTime startDate,
        LocalDateTime endDate,
        List<TransactionSummaryDTO> transactions,
        List<AccountSummaryDTO> accounts,
        List<CategorySummaryDTO> categories,
        List<SubcategorySummaryDTO> subcategories,
        TransactionOrder order) {
}