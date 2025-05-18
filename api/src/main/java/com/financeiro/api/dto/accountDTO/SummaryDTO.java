package com.financeiro.api.dto.accountDTO;

import java.time.LocalDateTime;
import java.util.List;

import com.financeiro.api.domain.enums.TransactionOrder;
import com.financeiro.api.dto.categoryDTO.CategorySummaryDTO;
import com.financeiro.api.dto.transactionDTO.TransactionSummaryDTO;

public record SummaryDTO(
    LocalDateTime start,
    LocalDateTime end,
    List<AccountSummaryDTO> accounts,
    List<CategorySummaryDTO> categories,
    List<TransactionSummaryDTO> transactions,
    TransactionOrder order
) {}
