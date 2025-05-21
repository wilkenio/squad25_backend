package com.financeiro.api.service;

import com.financeiro.api.domain.enums.TransactionState;
import com.financeiro.api.dto.accountDTO.AccountTransactionSummaryDTO;
import com.financeiro.api.dto.transactionDTO.TransactionFilterDTO;
import com.financeiro.api.dto.transactionDTO.TransactionRequestDTO;
import com.financeiro.api.dto.transactionDTO.TransactionResponseDTO;
import com.financeiro.api.dto.transactionDTO.TransactionSimplifiedResponseDTO;
import com.financeiro.api.dto.transactionDTO.RecurringUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    List<TransactionResponseDTO> create(TransactionRequestDTO dto);

    List<TransactionSimplifiedResponseDTO> findAll();

    TransactionSimplifiedResponseDTO findById(UUID id);

    TransactionResponseDTO updateState(UUID id, TransactionState state);

    TransactionResponseDTO update(UUID id, TransactionRequestDTO dto);

    void delete(UUID id);

    void cancelarRecorrencia(UUID recurringGroupId);

    void atualizarRecorrenciaFutura(UUID recurringGroupId, RecurringUpdateRequestDTO dto);
}
