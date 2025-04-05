package com.financeiro.api.service;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.User;
import com.financeiro.api.dto.accountDTO.AccountRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountResponseDTO;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public AccountResponseDTO create(AccountRequestDTO dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setAccountName(dto.accountName());
        account.setAccountDescription(dto.accountDescription());
        account.setAdditionalInformation(dto.additionalInformation());
        account.setOpeningBalance(dto.openingBalance());
        account.setSpecialCheck(dto.specialCheck());
        account.setStatus(dto.status());
        account.setCreatedAt(java.time.LocalDateTime.now());
        account.setUpdatedAt(java.time.LocalDateTime.now());

        Account saved = accountRepository.save(account);

        return new AccountResponseDTO(
                saved.getId(),
                saved.getUser().getId(),
                saved.getAccountName(),
                saved.getAccountDescription(),
                saved.getAdditionalInformation(),
                saved.getOpeningBalance(),
                saved.getSpecialCheck(),
                saved.getStatus()
        );
    }

    public List<AccountResponseDTO> getAll() {
        return accountRepository.findAll().stream()
                .map(acc -> new AccountResponseDTO(
                        acc.getId(),
                        acc.getUser().getId(),
                        acc.getAccountName(),
                        acc.getAccountDescription(),
                        acc.getAdditionalInformation(),
                        acc.getOpeningBalance(),
                        acc.getSpecialCheck(),
                        acc.getStatus()
                )).collect(Collectors.toList());
    }
}
