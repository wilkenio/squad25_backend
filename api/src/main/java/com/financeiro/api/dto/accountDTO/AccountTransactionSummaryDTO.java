package com.financeiro.api.dto.accountDTO;

import java.util.List;

import com.financeiro.api.dto.categoryDTO.CategorySummaryDTO;
import com.financeiro.api.dto.transactionDTO.TransferenciaDTO;

public record AccountTransactionSummaryDTO(
    String accountName,
    Double income,
    Double expectedIncomeMonth,
    Double expense,
    Double expectedExpenseMonth,
    List<TransferenciaDTO> transferencias,
    List<CategorySummaryDTO> categorias
) {}
