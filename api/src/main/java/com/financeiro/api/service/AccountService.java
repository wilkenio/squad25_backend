package com.financeiro.api.service;

import com.financeiro.api.dto.accountDTO.AccountRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    public AccountResponseDTO create(AccountRequestDTO dto);
    public List<AccountResponseDTO> getAll();
    public AccountResponseDTO findById(UUID id);
    public AccountResponseDTO update(UUID id, AccountRequestDTO dto);
    public void delete(UUID id);
}
