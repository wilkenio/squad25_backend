package com.financeiro.api.service;

import com.financeiro.api.dto.transactionDTO.TransactionResponseDTO;
import com.financeiro.api.dto.transferDTO.TransferRequestDTO;

import java.util.List;

public interface TransferService {
    List<TransactionResponseDTO> transfer(TransferRequestDTO dto);
}
