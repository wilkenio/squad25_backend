package com.financeiro.api.service;

import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.accountDTO.AccountCalculationRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountCalculationResponseDTO;
import com.financeiro.api.dto.accountDTO.AccountTransactionRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountTransactionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountCalculationResponseDTO create(AccountCalculationRequestDTO dto);

    List<AccountCalculationResponseDTO> findAll(Integer year, Integer month);

    AccountCalculationResponseDTO findById(UUID id, Integer year, Integer month);

    AccountTransactionResponseDTO update(UUID id, AccountTransactionRequestDTO dto); 

    void delete(UUID id);
    
    List<AccountCalculationResponseDTO> findByAccountName(String accountName, Integer year, Integer month);

    List<AccountCalculationResponseDTO> findByOpeningBalanceBetween(Double minValue, Double maxValue, Integer year, Integer month);

    List<AccountCalculationResponseDTO> findBySpecialCheckBetween(Double minValue, Double maxValue, Integer year, Integer month);

    List<AccountCalculationResponseDTO> findByStatus(Status status, Integer year, Integer month);

}