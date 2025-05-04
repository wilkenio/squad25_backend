package com.financeiro.api.service;

import com.financeiro.api.dto.transactionDTO.TransactionRequestDTO;
import com.financeiro.api.dto.transactionDTO.TransactionResponseDTO;
import com.financeiro.api.dto.transactionDTO.TransactionSimplifiedResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    public TransactionResponseDTO create(TransactionRequestDTO dto);
    public List<TransactionSimplifiedResponseDTO> findAll();
    public TransactionSimplifiedResponseDTO findById(UUID id);
    public void delete(UUID id);
}
