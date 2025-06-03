package com.financeiro.api.service;

import com.financeiro.api.domain.enums.TransactionState;
import com.financeiro.api.dto.transactionDTO.TransactionRequestDTO;
import com.financeiro.api.dto.transactionDTO.TransactionResponseDTO;
import com.financeiro.api.dto.transactionDTO.TransactionSimplifiedResponseDTO;
import com.financeiro.api.dto.transactionDTO.RecurringUpdateRequestDTO;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    List<TransactionResponseDTO> create(TransactionRequestDTO dto);

    List<TransactionSimplifiedResponseDTO> findAll(int page);

    TransactionSimplifiedResponseDTO findById(UUID id);

    TransactionResponseDTO updateState(UUID id, TransactionState state);

    void delete(UUID id);

    void cancelarRecorrencia(UUID recurringGroupId);

    TransactionResponseDTO update(UUID id, RecurringUpdateRequestDTO dto);

    void atualizarRecorrenciaFutura(UUID recurringGroupId, RecurringUpdateRequestDTO dto);
}
