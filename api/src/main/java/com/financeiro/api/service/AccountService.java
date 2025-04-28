package com.financeiro.api.service;

import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.accountDTO.AccountCalculationRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountCalculationResponseDTO;
import com.financeiro.api.dto.accountDTO.AccountRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountResponseDTO;
import com.financeiro.api.dto.accountDTO.AccountSaveResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    public List<AccountCalculationResponseDTO> getAll();
    public AccountCalculationResponseDTO findById(UUID id);
    public AccountSaveResponseDTO update(UUID id, AccountRequestDTO dto);
    public void delete(UUID id);
    
    public List<AccountCalculationResponseDTO> findByAccountName(String accountName);
    public List<AccountCalculationResponseDTO> findByOpeningBalanceBetween(Double minValue, Double maxValue);
    public List<AccountCalculationResponseDTO> findBySpecialCheckBetween(Double minValue, Double maxValue);
    public List<AccountCalculationResponseDTO> findByStatus(Status status);
    public AccountCalculationResponseDTO calculateAccountBalance(AccountCalculationRequestDTO dto);
}
