package com.financeiro.api.service;

import com.financeiro.api.dto.transactionDTO.TransactionRequestDTO;
import com.financeiro.api.dto.transactionDTO.TransactionResponseDTO;

import java.util.UUID;

public interface TransactionService {

    public TransactionResponseDTO create(TransactionRequestDTO dto);
    public TransactionResponseDTO findById(UUID id);
}
