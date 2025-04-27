package com.financeiro.api.service;

import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.accountDTO.AccountRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountResponseDTO;
import com.financeiro.api.dto.accountDTO.AccountSaveResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    public AccountSaveResponseDTO create(AccountRequestDTO dto);
    public List<AccountResponseDTO> getAll();
    public AccountResponseDTO findById(UUID id);
    public AccountSaveResponseDTO update(UUID id, AccountRequestDTO dto);
    public void delete(UUID id);
    
    public List<AccountResponseDTO> findByAccountName(String accountName);
    public List<AccountResponseDTO> findByOpeningBalanceBetween(Double minValue, Double maxValue);
    public List<AccountResponseDTO> findBySpecialCheckBetween(Double minValue, Double maxValue);
    public List<AccountResponseDTO> findByStatus(Status status);
}
