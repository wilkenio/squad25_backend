package com.financeiro.api.service;

import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.accountDTO.AccountCalculationRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountCalculationResponseDTO;
import com.financeiro.api.dto.accountDTO.AccountTransactionRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountTransactionResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    public AccountCalculationResponseDTO create(AccountCalculationRequestDTO dto);
    public List<AccountCalculationResponseDTO> findAll();
    public AccountCalculationResponseDTO findById(UUID id);
    public AccountTransactionResponseDTO update(UUID id, AccountTransactionRequestDTO dto);
    public void delete(UUID id);
    
    public List<AccountCalculationResponseDTO> findByAccountName(String accountName);
    public List<AccountCalculationResponseDTO> findByOpeningBalanceBetween(Double minValue, Double maxValue);
    public List<AccountCalculationResponseDTO> findBySpecialCheckBetween(Double minValue, Double maxValue);
    public List<AccountCalculationResponseDTO> findByStatus(Status status);
}
