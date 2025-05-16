package com.financeiro.api.dto.accountDTO;

import java.util.List;

import com.financeiro.api.dto.categoryDTO.CategoriaResumoDTO;
import com.financeiro.api.dto.transactionDTO.TransferenciaDTO;

public record AccountTransactionSummaryDTO(
    String accountName,
    Double income,
    Double expectedIncomeMonth,
    Double expense,
    Double expectedExpenseMonth,
    List<TransferenciaDTO> transferencias,
    List<CategoriaResumoDTO> categorias
) {}
