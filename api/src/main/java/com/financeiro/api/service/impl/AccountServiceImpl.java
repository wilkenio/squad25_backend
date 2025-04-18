package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.accountDTO.AccountRequestDTO;
import com.financeiro.api.dto.accountDTO.AccountResponseDTO;
import com.financeiro.api.infra.exceptions.UserNotFoundException;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.UserRepository;
import com.financeiro.api.service.AccountService;
import org.springframework.stereotype.Service;
import com.financeiro.api.domain.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService{

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public AccountServiceImpl(AccountRepository accountRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public AccountResponseDTO create(AccountRequestDTO dto) {
        User user = userRepository.findById(dto.userId()).orElseThrow(
                () -> new UserNotFoundException()
        );

        Category category = categoryRepository.findById(dto.categoryId()).orElseThrow(
                () -> new RuntimeException("Category not found")
        );

        Account account = new Account();
        account.setUser(user);
        account.setAccountName(dto.accountName());
        account.setCategory(category);
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
                saved.getCategory().getId(),
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
                        acc.getCategory() != null ? acc.getCategory().getId() : null,
                        acc.getAccountDescription(),
                        acc.getAdditionalInformation(),
                        acc.getOpeningBalance(),
                        acc.getSpecialCheck(),
                        acc.getStatus()
                )).collect(Collectors.toList());
    }

    @Override
    public AccountResponseDTO findById(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );

        return new AccountResponseDTO(
            account.getId(),
            account.getUser().getId(),
            account.getAccountName(),
            account.getAccountDescription(),
            account.getAdditionalInformation(),
            account.getOpeningBalance(),
            account.getSpecialCheck(),
            account.getStatus()
        );
    }

    @Override
    public AccountResponseDTO update(UUID id, AccountRequestDTO dto) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );

        User user = userRepository.findById(dto.userId()).orElseThrow(
                () -> new UserNotFoundException()
        );

        Category category = categoryRepository.findById(dto.categoryId()).orElseThrow(
                () -> new RuntimeException("Category not found")
        );

        account.setUser(user);
        account.setAccountName(dto.accountName());
        account.setCategory(category);
        account.setAccountDescription(dto.accountDescription());
        account.setAdditionalInformation(dto.additionalInformation());
        account.setOpeningBalance(dto.openingBalance());
        account.setSpecialCheck(dto.specialCheck());
        account.setStatus(dto.status());
        account.setUpdatedAt(LocalDateTime.now());

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

    @Override
    public void delete(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );

        account.setStatus(Status.EXC);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    @Override
    public List<AccountResponseDTO> findByAccountName(String accountName) {
        return accountRepository.findByAccountNameContainingIgnoreCase(accountName).stream()
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

    @Override
    public List<AccountResponseDTO> findByOpeningBalanceBetween(Double minValue, Double maxValue) {
        return accountRepository.findByOpeningBalanceBetween(minValue, maxValue).stream()
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

    @Override
    public List<AccountResponseDTO> findBySpecialCheckBetween(Double minValue, Double maxValue) {
        return accountRepository.findBySpecialCheckBetween(minValue, maxValue).stream()
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

    @Override
    public List<AccountResponseDTO> findByStatus(Status status) {
        return accountRepository.findByStatus(status).stream()
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