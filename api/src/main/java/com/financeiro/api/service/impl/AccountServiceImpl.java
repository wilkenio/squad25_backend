package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.domain.enums.TransactionType;
import com.financeiro.api.dto.accountDTO.*;
import com.financeiro.api.infra.exceptions.UserNotFoundException;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.UserRepository;
import com.financeiro.api.service.AccountService;
import org.springframework.stereotype.Service;

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

    //Adicionar as variáveis saldoInicial, receitas e despesas
    @Override//transformar no método GET
    public AccountCalculationResponseDTO calculateAccountBalance(AccountCalculationRequestDTO dto) {
        Double saldoInicial = dto.openingBalance();
        Double chequeEspecial = dto.specialCheck();

        // Buscar a categoria pelo nome
        String iconClass = "";
        String color = "";
        if (dto.categoryName() != null && !dto.categoryName().isEmpty()) {
            var categories = categoryRepository.findByNameContainingIgnoreCase(dto.categoryName());
            if (!categories.isEmpty()) {
                var category = categories.get(0);
                iconClass = category.getIconClass();
                color = category.getColor();
            }
        }

        // Declaração das variáveis para serem implementadas depois
        Double receitas = 0.0;
        Double despesas = 0.0;
        Double receitasPrevistas = 0.0;
        Double despesasPrevistas = 0.0;

        //Verificando se é uma despesa ou receita e fazendo a previsão do valor
        if (dto.transactions() != null) {
            for (var transaction : dto.transactions()) {
                if (transaction.type() == TransactionType.RECEITA) {
                    receitas += transaction.value();
                    receitasPrevistas = receitas * dto.openingBalanceMonth();
                } else if (transaction.type() == TransactionType.DESPESA) {
                    despesas += transaction.value();
                    despesasPrevistas = despesas * dto.openingBalanceMonth();
                }
            }
        }
        // Cálculo do saldo atual
        Double saldo = saldoInicial + receitas - despesas;

        // Cálculo do saldo previsto considerando as previsões mensais
        Double saldoPrevisto = saldo + receitasPrevistas - despesasPrevistas;

        // Cálculos totais incluindo o cheque especial
        Double receitaTotal = receitas + receitasPrevistas;
        Double despesaTotal = despesas + despesasPrevistas;
        Double saldoTotal = saldoPrevisto + chequeEspecial;

        //Salvando a conta no banco de dados antes de retornar
        Account account = new Account();
        account.setAccountName(dto.accountName());
        account.setAccountDescription(dto.accountDescription());
        account.setAdditionalInformation(dto.additionalInformation());
        account.setOpeningBalance(dto.openingBalance());
        account.setSpecialCheck(dto.specialCheck());
        account.setCreatedAt(java.time.LocalDateTime.now());
        account.setUpdatedAt(java.time.LocalDateTime.now());

        accountRepository.save(account);

        return new AccountCalculationResponseDTO(
                iconClass,
                color,
                dto.accountName(),
                dto.accountDescription(),
                dto.additionalInformation(),
                saldoInicial,
                receitas,
                despesas,
                receitasPrevistas,
                despesasPrevistas,
                saldo,
                saldoPrevisto,
                receitaTotal,
                despesaTotal,
                saldoTotal
        );
    }

    public List<AccountCalculationResponseDTO> getAll() {
        return accountRepository.findAll().stream()
                .map(acc -> {
                    String iconClass = "";
                    String color = "";
                    if (acc.getCategories() != null && !acc.getCategories().isEmpty()) {
                        var category = acc.getCategories().get(0);
                        iconClass = category.getIconClass();
                        color = category.getColor();
                    }
                    
                    Double saldoInicial = acc.getOpeningBalance();
                    Double receitas = 0.0;
                    Double despesas = 0.0;
                    Double receitasPrevistas = 0.0;
                    Double despesasPrevistas = 0.0;
                    Double saldo = saldoInicial + receitas - despesas;
                    Double saldoPrevisto = saldo + receitasPrevistas - despesasPrevistas;
                    Double receitaTotal = receitas + receitasPrevistas;
                    Double despesaTotal = despesas + despesasPrevistas;
                    Double saldoTotal = saldoPrevisto + acc.getSpecialCheck();

                    return new AccountCalculationResponseDTO(
                            iconClass,
                            color,
                            acc.getAccountName(),
                            acc.getAccountDescription(),
                            acc.getAdditionalInformation(),
                            saldoInicial,
                            receitas,
                            despesas,
                            receitasPrevistas,
                            despesasPrevistas,
                            saldo,
                            saldoPrevisto,
                            receitaTotal,
                            despesaTotal,
                            saldoTotal
                    );
                }).collect(Collectors.toList());
    }

    @Override
    public AccountCalculationResponseDTO findById(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );

        String iconClass = "";
        String color = "";
        if (account.getCategories() != null && !account.getCategories().isEmpty()) {
            var category = account.getCategories().get(0);
            iconClass = category.getIconClass();
            color = category.getColor();
        }

        Double saldoInicial = account.getOpeningBalance();
        Double receitas = 0.0;
        Double despesas = 0.0;
        Double receitasPrevistas = 0.0;
        Double despesasPrevistas = 0.0;
        Double saldo = saldoInicial + receitas - despesas;
        Double saldoPrevisto = saldo + receitasPrevistas - despesasPrevistas;
        Double receitaTotal = receitas + receitasPrevistas;
        Double despesaTotal = despesas + despesasPrevistas;
        Double saldoTotal = saldoPrevisto + account.getSpecialCheck();

        return new AccountCalculationResponseDTO(
                iconClass,
                color,
                account.getAccountName(),
                account.getAccountDescription(),
                account.getAdditionalInformation(),
                saldoInicial,
                receitas,
                despesas,
                receitasPrevistas,
                despesasPrevistas,
                saldo,
                saldoPrevisto,
                receitaTotal,
                despesaTotal,
                saldoTotal
        );
    }

    @Override
    public AccountSaveResponseDTO update(UUID id, AccountRequestDTO dto) {
        Account account = accountRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException()
        );

        account.setAccountName(dto.accountName());
        account.setAccountDescription(dto.accountDescription());
        account.setAdditionalInformation(dto.additionalInformation());
        account.setOpeningBalance(dto.openingBalance());
        account.setSpecialCheck(dto.specialCheck());
        account.setUpdatedAt(LocalDateTime.now());

        Account saved = accountRepository.save(account);
        return new AccountSaveResponseDTO(
                saved.getId(),
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
    public List<AccountCalculationResponseDTO> findByAccountName(String accountName) {
        return accountRepository.findByAccountNameContainingIgnoreCase(accountName).stream()
                .map(acc -> {
                    String iconClass = "";
                    String color = "";
                    if (acc.getCategories() != null && !acc.getCategories().isEmpty()) {
                        var category = acc.getCategories().get(0);
                        iconClass = category.getIconClass();
                        color = category.getColor();
                    }

                    Double saldoInicial = acc.getOpeningBalance();
                    Double receitas = 0.0;
                    Double despesas = 0.0;
                    Double receitasPrevistas = 0.0;
                    Double despesasPrevistas = 0.0;
                    Double saldo = saldoInicial + receitas - despesas;
                    Double saldoPrevisto = saldo + receitasPrevistas - despesasPrevistas;
                    Double receitaTotal = receitas + receitasPrevistas;
                    Double despesaTotal = despesas + despesasPrevistas;
                    Double saldoTotal = saldoPrevisto + acc.getSpecialCheck();

                    return new AccountCalculationResponseDTO(
                            iconClass,
                            color,
                            acc.getAccountName(),
                            acc.getAccountDescription(),
                            acc.getAdditionalInformation(),
                            saldoInicial,
                            receitas,
                            despesas,
                            receitasPrevistas,
                            despesasPrevistas,
                            saldo,
                            saldoPrevisto,
                            receitaTotal,
                            despesaTotal,
                            saldoTotal
                    );
                }).collect(Collectors.toList());
    }

    @Override
    public List<AccountCalculationResponseDTO> findByOpeningBalanceBetween(Double minValue, Double maxValue) {
        return accountRepository.findByOpeningBalanceBetween(minValue, maxValue).stream()
                .map(acc -> {
                    String iconClass = "";
                    String color = "";
                    if (acc.getCategories() != null && !acc.getCategories().isEmpty()) {
                        var category = acc.getCategories().get(0);
                        iconClass = category.getIconClass();
                        color = category.getColor();
                    }

                    Double saldoInicial = acc.getOpeningBalance();
                    Double receitas = 0.0;
                    Double despesas = 0.0;
                    Double receitasPrevistas = 0.0;
                    Double despesasPrevistas = 0.0;
                    Double saldo = saldoInicial + receitas - despesas;
                    Double saldoPrevisto = saldo + receitasPrevistas - despesasPrevistas;
                    Double receitaTotal = receitas + receitasPrevistas;
                    Double despesaTotal = despesas + despesasPrevistas;
                    Double saldoTotal = saldoPrevisto + acc.getSpecialCheck();

                    return new AccountCalculationResponseDTO(
                            iconClass,
                            color,
                            acc.getAccountName(),
                            acc.getAccountDescription(),
                            acc.getAdditionalInformation(),
                            saldoInicial,
                            receitas,
                            despesas,
                            receitasPrevistas,
                            despesasPrevistas,
                            saldo,
                            saldoPrevisto,
                            receitaTotal,
                            despesaTotal,
                            saldoTotal
                    );
                }).collect(Collectors.toList());
    }

    @Override
    public List<AccountCalculationResponseDTO> findBySpecialCheckBetween(Double minValue, Double maxValue) {
        return accountRepository.findBySpecialCheckBetween(minValue, maxValue).stream()
                .map(acc -> {
                    String iconClass = "";
                    String color = "";
                    if (acc.getCategories() != null && !acc.getCategories().isEmpty()) {
                        var category = acc.getCategories().get(0);
                        iconClass = category.getIconClass();
                        color = category.getColor();
                    }

                    Double saldoInicial = acc.getOpeningBalance();
                    Double receitas = 0.0;
                    Double despesas = 0.0;
                    Double receitasPrevistas = 0.0;
                    Double despesasPrevistas = 0.0;
                    Double saldo = saldoInicial + receitas - despesas;
                    Double saldoPrevisto = saldo + receitasPrevistas - despesasPrevistas;
                    Double receitaTotal = receitas + receitasPrevistas;
                    Double despesaTotal = despesas + despesasPrevistas;
                    Double saldoTotal = saldoPrevisto + acc.getSpecialCheck();

                    return new AccountCalculationResponseDTO(
                            iconClass,
                            color,
                            acc.getAccountName(),
                            acc.getAccountDescription(),
                            acc.getAdditionalInformation(),
                            saldoInicial,
                            receitas,
                            despesas,
                            receitasPrevistas,
                            despesasPrevistas,
                            saldo,
                            saldoPrevisto,
                            receitaTotal,
                            despesaTotal,
                            saldoTotal
                    );
                }).collect(Collectors.toList());
    }

    @Override
    public List<AccountCalculationResponseDTO> findByStatus(Status status) {
        return accountRepository.findByStatus(status).stream()
                .map(acc -> {
                    String iconClass = "";
                    String color = "";
                    if (acc.getCategories() != null && !acc.getCategories().isEmpty()) {
                        var category = acc.getCategories().get(0);
                        iconClass = category.getIconClass();
                        color = category.getColor();
                    }

                    Double saldoInicial = acc.getOpeningBalance();
                    Double receitas = 0.0;
                    Double despesas = 0.0;
                    Double receitasPrevistas = 0.0;
                    Double despesasPrevistas = 0.0;
                    Double saldo = saldoInicial + receitas - despesas;
                    Double saldoPrevisto = saldo + receitasPrevistas - despesasPrevistas;
                    Double receitaTotal = receitas + receitasPrevistas;
                    Double despesaTotal = despesas + despesasPrevistas;
                    Double saldoTotal = saldoPrevisto + acc.getSpecialCheck();

                    return new AccountCalculationResponseDTO(
                            iconClass,
                            color,
                            acc.getAccountName(),
                            acc.getAccountDescription(),
                            acc.getAdditionalInformation(),
                            saldoInicial,
                            receitas,
                            despesas,
                            receitasPrevistas,
                            despesasPrevistas,
                            saldo,
                            saldoPrevisto,
                            receitaTotal,
                            despesaTotal,
                            saldoTotal
                    );
                }).collect(Collectors.toList());
    }
}