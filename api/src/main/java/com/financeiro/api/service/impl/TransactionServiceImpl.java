package com.financeiro.api.service.impl;

import com.financeiro.api.domain.*;
import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Subcategory;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.dto.accountDTO.AccountTransactionSummaryDTO;
import com.financeiro.api.dto.transactionDTO.*;
import com.financeiro.api.infra.exceptions.TransactionNotFoundException;
import com.financeiro.api.repository.*;
import com.financeiro.api.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

        private final TransactionRepository transactionRepository;
        private final AccountRepository accountRepository;
        private final CategoryRepository categoryRepository;
        private final SubcategoryRepository subcategoryRepository;
        private final AccountServiceImpl accountService;

        public TransactionServiceImpl(TransactionRepository repository,
                        AccountRepository accountRepository,
                        CategoryRepository categoryRepository,
                        SubcategoryRepository subcategoryRepository,
                        AccountServiceImpl accountService) {
                this.transactionRepository = repository;
                this.accountRepository = accountRepository;
                this.categoryRepository = categoryRepository;
                this.subcategoryRepository = subcategoryRepository;
                this.accountService = accountService;
        }

        @Override
        public TransactionResponseDTO create(TransactionRequestDTO dto) {

                Account account = accountRepository.findById(dto.accounId())
                                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

                accountService.updateAccountByTransaction(account.getId(), dto.type(), dto.value());

                Category category = categoryRepository.findById(dto.categoryId())
                                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

                Subcategory subcategory = subcategoryRepository.findById(dto.subcategoryId())
                                .orElseThrow(
                                                () -> new EntityNotFoundException(
                                                                "Subcategoria não encontrada ou não pertence à categoria selecionada"));

                Transaction transaction = new Transaction();
                transaction.setAccount(account);
                transaction.setCategory(category);
                transaction.setSubcategory(subcategory);
                transaction.setName(dto.name());
                transaction.setType(dto.type());
                transaction.setStatus(dto.status());
                transaction.setReleaseDate(LocalDateTime.now());
                transaction.setValue(dto.value());
                transaction.setDescription(dto.description());
                transaction.setState(dto.state());
                transaction.setAdditionalInformation(dto.additionalInformation());
                transaction.setFrequency(dto.frequency());
                transaction.setInstallments(dto.installments());

                LocalDateTime now = java.time.LocalDateTime.now();
                transaction.setCreatedAt(now);
                transaction.setUpdatedAt(now);

                // Salva a transação e retorna o DTO
                Transaction savedTransaction = transactionRepository.save(transaction);
                return toDTO(savedTransaction);
        }

        @Override
        public List<TransactionSimplifiedResponseDTO> findAll() {
                List<Status> statuses = List.of(Status.SIM, Status.NAO);
                return transactionRepository.findAllByStatusIn(statuses).stream()
                                .map(transaction -> new TransactionSimplifiedResponseDTO(
                                                transaction.getId(),
                                                transaction.getName(),
                                                transaction.getDescription(),
                                                transaction.getType(),
                                                transaction.getAccount().getAccountName(),
                                                transaction.getFrequency(),
                                                transaction.getValue()))
                                .toList();
        }

        @Override
        public TransactionSimplifiedResponseDTO findById(UUID id) {
                return transactionRepository.findById(id)
                                .map(transaction -> new TransactionSimplifiedResponseDTO(
                                                transaction.getId(),
                                                transaction.getName(),
                                                transaction.getDescription(),
                                                transaction.getType(),
                                                transaction.getAccount().getAccountName(),
                                                transaction.getFrequency(),
                                                transaction.getValue()))
                                .orElseThrow(() -> new TransactionNotFoundException());
        }

        @Override
        public void delete(UUID id) {
                Transaction transaction = transactionRepository.findById(id)
                                .orElseThrow(() -> new TransactionNotFoundException());

                transaction.setStatus(Status.EXC);
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
        }

        private TransactionResponseDTO toDTO(Transaction transaction) {
                return new TransactionResponseDTO(
                                transaction.getId(),
                                transaction.getAccount().getId(),
                                transaction.getAccount().getAccountName(),
                                transaction.getCategory().getId(),
                                transaction.getCategory().getName(),
                                transaction.getCategory().getIconClass(),
                                transaction.getCategory().getColor(),
                                transaction.getSubcategory().getId(),
                                transaction.getSubcategory().getName(),
                                transaction.getSubcategory().getIconClass(),
                                transaction.getSubcategory().getColor(),
                                transaction.getName(),
                                transaction.getType(),
                                transaction.getStatus(),
                                transaction.getReleaseDate(),
                                transaction.getValue(),
                                transaction.getDescription(),
                                transaction.getState(),
                                transaction.getAdditionalInformation(),
                                transaction.getFrequency(),
                                transaction.getInstallments());
        }

        public List<AccountTransactionSummaryDTO> filtrarTransacoes(TransactionFilterDTO filtro) {
                // Busca todas as transações conforme os filtros básicos
                List<Transaction> transacoes = transactionRepository.findAll().stream()
                    .filter(t -> filtro.contaIds() == null || filtro.contaIds().isEmpty() || filtro.contaIds().contains(t.getAccount().getId()))
                    .filter(t -> filtro.categoriaIds() == null || filtro.categoriaIds().isEmpty() || filtro.categoriaIds().contains(t.getCategory().getId()))
                    .filter(t -> (filtro.dataInicio() == null || !t.getReleaseDate().isBefore(filtro.dataInicio())) &&
                                 (filtro.dataFim() == null || !t.getReleaseDate().isAfter(filtro.dataFim())))
                    .collect(Collectors.toList());
            
                // Ordenação conforme TransactionOrder
                Comparator<Transaction> comparator = Comparator.comparing(Transaction::getReleaseDate); // padrão: DATA
                if (filtro.ordenacao() != null) {
                    switch (filtro.ordenacao()) {
                        case DATA:
                            comparator = Comparator.comparing(Transaction::getReleaseDate);
                            break;
                        case CATEGORIA:
                            comparator = Comparator.comparing(t -> t.getCategory().getName(), String.CASE_INSENSITIVE_ORDER);
                            break;
                        case VALOR_CRESCENTE:
                            comparator = Comparator.comparing(Transaction::getValue);
                            break;
                        case VALOR_DECRESCENTE:
                            comparator = Comparator.comparing(Transaction::getValue).reversed();
                            break;
                    }
                }
            
                // Ordena e limita a 10 resultados
                List<Transaction> transacoesOrdenadas = transacoes.stream()
                    .sorted(comparator)
                    .limit(10)
                    .collect(Collectors.toList());
            
                // Monta o DTO de retorno (ajuste conforme sua lógica de agregação)
                // Aqui, para cada transação, retorna um resumo da conta (pode ser ajustado para agrupar por conta, se necessário)
                return transacoesOrdenadas.stream()
                    .map(t -> new AccountTransactionSummaryDTO(
                        t.getAccount().getAccountName(),
                        t.getType() != null && t.getType().name().equals("INCOME") ? t.getValue() : null,
                        null, // expectedIncomeMonth (ajuste se necessário)
                        t.getType() != null && t.getType().name().equals("EXPENSE") ? t.getValue() : null,
                        null, // expectedExpenseMonth (ajuste se necessário)
                        List.of(), // transferencias (ajuste se necessário)
                        List.of()  // categorias (ajuste se necessário)
                    ))
                    .collect(Collectors.toList());
        }
}
