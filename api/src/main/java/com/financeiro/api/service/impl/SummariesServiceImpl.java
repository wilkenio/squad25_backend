package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.dashboardDTO.AccountBalanceDashboardDTO;
import com.financeiro.api.dto.dashboardDTO.CategorySummaryDashboardDTO;
import com.financeiro.api.dto.dashboardDTO.DashboardItemDTO;
import com.financeiro.api.dto.dashboardDTO.TransactionDashboardDTO;
import com.financeiro.api.dto.transactionDTO.TransactionAdvancedFilterDTO;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.service.SummariesService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class SummariesServiceImpl implements SummariesService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public SummariesServiceImpl(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<DashboardItemDTO> generateSummary(TransactionAdvancedFilterDTO filtro) {
        List<DashboardItemDTO> dtosDeSaldoCalculados = new ArrayList<>();

        if (Boolean.TRUE.equals(filtro.mostrarApenasSaldo())) {
            boolean incluirPrevisao = Boolean.TRUE.equals(filtro.incluirSaldoPrevisto());

            if (filtro.contaIds() != null && !filtro.contaIds().isEmpty()) {
                
                List<Account> accountsParaSaldo = new ArrayList<>();
                for (UUID accountId : filtro.contaIds()) {
                    accountRepository.findById(accountId).ifPresent(account -> {
                        if (account.getStatus() != Status.EXC) {
                            accountsParaSaldo.add(account);
                        }
                    });
                }

                for (Account account : accountsParaSaldo) {
                    Double vSaldoInicial = Optional.ofNullable(account.getOpeningBalance()).orElse(0.0);
                    Double vReceitas = Optional.ofNullable(account.getIncome()).orElse(0.0);
                    Double vDespesas = Optional.ofNullable(account.getExpense()).orElse(0.0);
                    Double vChequeEspecial = Optional.ofNullable(account.getSpecialCheck()).orElse(0.0);
                    Double vReceitasPrevistas = Optional.ofNullable(account.getExpectedIncomeMonth()).orElse(0.0);
                    Double vDespesasPrevistas = Optional.ofNullable(account.getExpectedExpenseMonth()).orElse(0.0);

                    Double saldoAtualCalculado = vSaldoInicial + vReceitas - vDespesas;
                    dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(
                            account.getId(), account.getAccountName(), saldoAtualCalculado, "Saldo Atual"
                    ));

                    if (incluirPrevisao) {
                        Double saldoPrevistoCalculado = saldoAtualCalculado + vChequeEspecial + vReceitasPrevistas - vDespesasPrevistas;
                        dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(
                                account.getId(), account.getAccountName(), saldoPrevistoCalculado, "Saldo Previsto"
                        ));
                    }
                }
            } else {

                List<Account> todasContasAtivas = accountRepository.findAllByStatusIn(List.of(Status.SIM, Status.NAO));
                double somaDosSaldosAtuaisConsolidados = 0.0;
                double somaDosSaldosPrevistosConsolidados = 0.0;

                if (!todasContasAtivas.isEmpty()) {
                    for (Account account : todasContasAtivas) {
                        Double vSaldoInicial = Optional.ofNullable(account.getOpeningBalance()).orElse(0.0);
                        Double vReceitas = Optional.ofNullable(account.getIncome()).orElse(0.0);
                        Double vDespesas = Optional.ofNullable(account.getExpense()).orElse(0.0);
                        
                        Double saldoAtualIndividual = vSaldoInicial + vReceitas - vDespesas;
                        somaDosSaldosAtuaisConsolidados += saldoAtualIndividual;

                        if (incluirPrevisao) {
                            Double vChequeEspecial = Optional.ofNullable(account.getSpecialCheck()).orElse(0.0);
                            Double vReceitasPrevistas = Optional.ofNullable(account.getExpectedIncomeMonth()).orElse(0.0);
                            Double vDespesasPrevistas = Optional.ofNullable(account.getExpectedExpenseMonth()).orElse(0.0);
                            Double saldoPrevistoIndividual = saldoAtualIndividual + vChequeEspecial + vReceitasPrevistas - vDespesasPrevistas;
                            somaDosSaldosPrevistosConsolidados += saldoPrevistoIndividual;
                        }
                    }
                    dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(null, "Saldo Consolidado Total", somaDosSaldosAtuaisConsolidados, "Soma dos Saldos Atuais de Todas as Contas"));
                    if (incluirPrevisao) {
                        dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(null, "Saldo Consolidado Total (Previsto)", somaDosSaldosPrevistosConsolidados, "Soma dos Saldos Previstos de Todas as Contas"));
                    }
                } else { 
                    dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(null, "Saldo Consolidado Total", 0.0, "Nenhuma conta ativa para consolidar"));
                    if (incluirPrevisao) {
                    dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(null, "Saldo Consolidado Total (Previsto)", 0.0, "Nenhuma conta ativa para consolidar previsão"));
                    }
                }
            }
        }

        List<DashboardItemDTO> resultadosPrincipais = new ArrayList<>();
        TipoDado tipoDadoPrincipal = filtro.tipoDado() != null ? filtro.tipoDado() : TipoDado.TRANSACAO;

        Stream<Transaction> transactionStreamBase = transactionRepository.findAll().stream()

            .filter(t -> filtro.contaIds() == null || filtro.contaIds().isEmpty() || 
                        (t.getAccount() != null && filtro.contaIds().contains(t.getAccount().getId())))

            .filter(transaction -> {
                Category category = transaction.getCategory();
                boolean filtroCategoriaReceitaAtivado = Boolean.TRUE.equals(filtro.incluirTodasCategoriasReceita()) ||
                                                    (filtro.idsCategoriasReceita() != null && !filtro.idsCategoriasReceita().isEmpty());
                boolean filtroCategoriaDespesaAtivado = Boolean.TRUE.equals(filtro.incluirTodasCategoriasDespesa()) ||
                                                    (filtro.idsCategoriasDespesa() != null && !filtro.idsCategoriasDespesa().isEmpty());

                if (!filtroCategoriaReceitaAtivado && !filtroCategoriaDespesaAtivado) return true;
                if (category == null) return false;

                boolean matchesRevenueCriteria = false;
                if (filtroCategoriaReceitaAtivado && category.getType() == CategoryType.REVENUE) {
                    if (Boolean.TRUE.equals(filtro.incluirTodasCategoriasReceita()) || 
                        (filtro.idsCategoriasReceita() != null && filtro.idsCategoriasReceita().contains(category.getId()))) {
                        matchesRevenueCriteria = true;
                    }
                }

                boolean matchesExpenseCriteria = false;
                if (filtroCategoriaDespesaAtivado && category.getType() == CategoryType.EXPENSE) {
                    if (Boolean.TRUE.equals(filtro.incluirTodasCategoriasDespesa()) ||
                        (filtro.idsCategoriasDespesa() != null && filtro.idsCategoriasDespesa().contains(category.getId()))) {
                        matchesExpenseCriteria = true;
                    }
                }
                return matchesRevenueCriteria || matchesExpenseCriteria;
            })

            .filter(transaction -> {
                Frequency transactionFrequency = transaction.getFrequency();
                Periodicity transactionPeriodicity = transaction.getPeriodicity();
                boolean isNaoRecorrente = transactionFrequency == Frequency.NON_RECURRING;
                boolean isRepetida = transactionFrequency == Frequency.REPEAT;
                boolean isFixaMensal = isRepetida && transactionPeriodicity == Periodicity.MENSAL;
                boolean checkNaoRecorrente = Boolean.TRUE.equals(filtro.incluirFreqNaoRecorrente());
                boolean checkRepetida = Boolean.TRUE.equals(filtro.incluirFreqRepetida());
                boolean checkFixaMensal = Boolean.TRUE.equals(filtro.incluirFreqFixaMensal());
                if (!checkNaoRecorrente && !checkRepetida && !checkFixaMensal) return true;
                if (checkNaoRecorrente && isNaoRecorrente) return true;
                if (checkRepetida && isRepetida) return true;
                if (checkFixaMensal && isFixaMensal) return true;
                return false;
            })

            .filter(t -> {
                LocalDateTime dataComparacao;
                DataReferencia dataReferencia = filtro.dataReferencia() != null ? filtro.dataReferencia() : DataReferencia.LANCAMENTO;
                if (dataReferencia == DataReferencia.LANCAMENTO) dataComparacao = t.getCreatedAt(); else dataComparacao = t.getReleaseDate();
                if (dataComparacao == null) return false;
                return (filtro.dataInicio() == null || !dataComparacao.isBefore(filtro.dataInicio())) &&
                    (filtro.dataFim() == null || !dataComparacao.isAfter(filtro.dataFim()));
            })

            .filter(transaction -> {
                boolean isEfetivada = transaction.getState() == TransactionState.EFFECTIVE;
                boolean isPrevista = transaction.getState() == TransactionState.PENDING;
                boolean isActualTransfer = transaction.getTransferGroupId() != null;
                boolean isReceitaType = transaction.getType() == TransactionType.RECEITA;
                boolean isDespesaType = transaction.getType() == TransactionType.DESPESA;
                boolean anyPrimaryTypeFilterActive = Boolean.TRUE.equals(filtro.incluirReceitas()) ||
                                                    Boolean.TRUE.equals(filtro.incluirDespesas()) ||
                                                    Boolean.TRUE.equals(filtro.incluirTransferencias());
                if (!anyPrimaryTypeFilterActive) return true;

                if (Boolean.TRUE.equals(filtro.incluirReceitas()) && isReceitaType && !isActualTransfer) {
                    boolean cE = Boolean.TRUE.equals(filtro.incluirReceitasEfetivadas()); boolean cP = Boolean.TRUE.equals(filtro.incluirReceitasPrevistas());
                    if (!cE && !cP) return true; if (cE && isEfetivada) return true; if (cP && isPrevista) return true;
                }
                if (Boolean.TRUE.equals(filtro.incluirDespesas()) && isDespesaType && !isActualTransfer) {
                    boolean cE = Boolean.TRUE.equals(filtro.incluirDespesasEfetivadas()); boolean cP = Boolean.TRUE.equals(filtro.incluirDespesasPrevistas());
                    if (!cE && !cP) return true; if (cE && isEfetivada) return true; if (cP && isPrevista) return true;
                }
                if (Boolean.TRUE.equals(filtro.incluirTransferencias()) && isActualTransfer) {
                    boolean cE = Boolean.TRUE.equals(filtro.incluirTransferenciasEfetivadas()); boolean cP = Boolean.TRUE.equals(filtro.incluirTransferenciasPrevistas());
                    if (!cE && !cP) return true; if (cE && isEfetivada) return true; if (cP && isPrevista) return true;
                }
                return false;
            });

        if (tipoDadoPrincipal == TipoDado.CATEGORIA) {
            List<Transaction> filteredTransactionsForCategory = transactionStreamBase.toList();
            int limite = (filtro.limite() != null && filtro.limite() > 0) ? filtro.limite() : 20;
            Map<Category, Double> categorySums = filteredTransactionsForCategory.stream()
                    .filter(t -> t.getCategory() != null) 
                    .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getValue)));
            
            List<CategoryTotal> categoryTotalsList = categorySums.entrySet().stream()
                    .map(entry -> new CategoryTotal(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            boolean filtroCategoriaReceitaAtivado = Boolean.TRUE.equals(filtro.incluirTodasCategoriasReceita()) ||
                                                (filtro.idsCategoriasReceita() != null && !filtro.idsCategoriasReceita().isEmpty());
            boolean filtroCategoriaDespesaAtivado = Boolean.TRUE.equals(filtro.incluirTodasCategoriasDespesa()) ||
                                                (filtro.idsCategoriasDespesa() != null && !filtro.idsCategoriasDespesa().isEmpty());
            if (!filtroCategoriaReceitaAtivado && !filtroCategoriaDespesaAtivado) {
                double uncategorizedSum = transactionRepository.findAll().stream()
                                        .filter(t -> filtro.contaIds() == null || filtro.contaIds().isEmpty() || (t.getAccount() != null && filtro.contaIds().contains(t.getAccount().getId())))
                                        .filter(t -> t.getCategory() == null)
                                        .filter(t -> { LocalDateTime dataComparacao; DataReferencia dataReferencia = filtro.dataReferencia() != null ? filtro.dataReferencia() : DataReferencia.LANCAMENTO; if (dataReferencia == DataReferencia.LANCAMENTO) dataComparacao = t.getCreatedAt(); else dataComparacao = t.getReleaseDate(); if (dataComparacao == null) return false; return (filtro.dataInicio() == null || !dataComparacao.isBefore(filtro.dataInicio())) && (filtro.dataFim() == null || !dataComparacao.isAfter(filtro.dataFim()));})
                                        .filter(transaction -> { boolean isEfetivada = transaction.getState() == TransactionState.EFFECTIVE; boolean isPrevista = transaction.getState() == TransactionState.PENDING; boolean isActualTransfer = transaction.getTransferGroupId() != null; boolean isReceitaType = transaction.getType() == TransactionType.RECEITA; boolean isDespesaType = transaction.getType() == TransactionType.DESPESA; boolean anyPrimaryTypeFilterActive = Boolean.TRUE.equals(filtro.incluirReceitas()) || Boolean.TRUE.equals(filtro.incluirDespesas()) || Boolean.TRUE.equals(filtro.incluirTransferencias()); if (!anyPrimaryTypeFilterActive) return true; if (Boolean.TRUE.equals(filtro.incluirReceitas()) && isReceitaType && !isActualTransfer) { boolean cE = Boolean.TRUE.equals(filtro.incluirReceitasEfetivadas()); boolean cP = Boolean.TRUE.equals(filtro.incluirReceitasPrevistas()); if (!cE && !cP) return true; if (cE && isEfetivada) return true; if (cP && isPrevista) return true; } if (Boolean.TRUE.equals(filtro.incluirDespesas()) && isDespesaType && !isActualTransfer) { boolean cE = Boolean.TRUE.equals(filtro.incluirDespesasEfetivadas()); boolean cP = Boolean.TRUE.equals(filtro.incluirDespesasPrevistas()); if (!cE && !cP) return true; if (cE && isEfetivada) return true; if (cP && isPrevista) return true; } if (Boolean.TRUE.equals(filtro.incluirTransferencias()) && isActualTransfer) { boolean cE = Boolean.TRUE.equals(filtro.incluirTransferenciasEfetivadas()); boolean cP = Boolean.TRUE.equals(filtro.incluirTransferenciasPrevistas()); if (!cE && !cP) return true; if (cE && isEfetivada) return true; if (cP && isPrevista) return true; } return false;})
                                        .filter(transaction -> { Frequency transactionFrequency = transaction.getFrequency(); Periodicity transactionPeriodicity = transaction.getPeriodicity(); boolean isNaoRecorrente = transactionFrequency == Frequency.NON_RECURRING; boolean isRepetida = transactionFrequency == Frequency.REPEAT; boolean isFixaMensal = isRepetida && transactionPeriodicity == Periodicity.MENSAL; boolean cNR = Boolean.TRUE.equals(filtro.incluirFreqNaoRecorrente()); boolean cR = Boolean.TRUE.equals(filtro.incluirFreqRepetida()); boolean cFM = Boolean.TRUE.equals(filtro.incluirFreqFixaMensal()); if (!cNR && !cR && !cFM) return true; if (cNR && isNaoRecorrente) return true; if (cR && isRepetida) return true; if (cFM && isFixaMensal) return true; return false;})
                                        .mapToDouble(Transaction::getValue)
                                        .sum();
                if (uncategorizedSum != 0) {
                    categoryTotalsList.add(new CategoryTotal(null, uncategorizedSum));
                }
            }
            
            TipoApresentacaoDados apresentacaoCat = filtro.apresentacao() != null ? filtro.apresentacao() : TipoApresentacaoDados.TODOS;
            switch (apresentacaoCat) {
                case PRIMEIROS: resultadosPrincipais.addAll(categoryTotalsList.stream().sorted(Comparator.comparing(CategoryTotal::totalValue).reversed()).limit(limite).map(this::categoryTotalToDashboardDTO).collect(Collectors.toList())); break;
                case ULTIMOS: resultadosPrincipais.addAll(categoryTotalsList.stream().sorted(Comparator.comparing(CategoryTotal::totalValue)).limit(limite).map(this::categoryTotalToDashboardDTO).collect(Collectors.toList())); break;
                case SOMA: 
                    double totalSumForCategories = filteredTransactionsForCategory.stream().mapToDouble(Transaction::getValue).sum(); 
                    resultadosPrincipais.add(new CategorySummaryDashboardDTO(null, "Soma Total (Resultado da Categoria)", totalSumForCategories)); 
                    break;
                case TODOS: default:
                    Comparator<CategoryTotal> todosComparator;
                    TransactionOrder orderCat = filtro.ordenacao();
                    if (orderCat == TransactionOrder.VALOR_CRESCENTE) todosComparator = Comparator.comparing(CategoryTotal::totalValue);
                    else if (orderCat == TransactionOrder.VALOR_DECRESCENTE) todosComparator = Comparator.comparing(CategoryTotal::totalValue).reversed();
                    else todosComparator = Comparator.comparing(ct -> (ct.category() != null && ct.category().getName() != null) ? ct.category().getName() : "Sem Categoria", String.CASE_INSENSITIVE_ORDER);
                    resultadosPrincipais.addAll(categoryTotalsList.stream().sorted(todosComparator).map(this::categoryTotalToDashboardDTO).collect(Collectors.toList())); break;
            }

        } else if (tipoDadoPrincipal == TipoDado.TRANSACAO) {
            List<Transaction> filteredTransactions = transactionStreamBase.toList();
            Comparator<Transaction> transactionComparator = obterComparador(filtro.ordenacao());
            List<Transaction> sortedTransactions = filteredTransactions.stream().sorted(transactionComparator).toList();
            int limite = (filtro.limite() != null && filtro.limite() > 0) ? filtro.limite() : 20;
            TipoApresentacaoDados apresentacao = filtro.apresentacao() != null ? filtro.apresentacao() : TipoApresentacaoDados.TODOS;
            switch (apresentacao) {
                case PRIMEIROS: resultadosPrincipais.addAll(sortedTransactions.stream().limit(limite).map(this::transactionToDashboardDTO).collect(Collectors.toList())); break;
                case ULTIMOS: resultadosPrincipais.addAll(filteredTransactions.stream().sorted(transactionComparator.reversed()).limit(limite).map(this::transactionToDashboardDTO).collect(Collectors.toList())); break;
                case SOMA: 
                    double totalSumTransactions = sortedTransactions.stream().mapToDouble(Transaction::getValue).sum(); 
                    resultadosPrincipais.add(new CategorySummaryDashboardDTO(null, "Soma Total (Resultado da Transação)", totalSumTransactions)); 
                    break;
                case TODOS: default: resultadosPrincipais.addAll(sortedTransactions.stream().map(this::transactionToDashboardDTO).collect(Collectors.toList())); break;
            }
        }

        List<DashboardItemDTO> respostaFinalCombinada = new ArrayList<>();
        respostaFinalCombinada.addAll(dtosDeSaldoCalculados); 
        respostaFinalCombinada.addAll(resultadosPrincipais);

        return respostaFinalCombinada;
    }

    private record CategoryTotal(Category category, double totalValue) {}

    private CategorySummaryDashboardDTO categoryTotalToDashboardDTO(CategoryTotal ct) {
        return new CategorySummaryDashboardDTO(
            ct.category() != null ? ct.category().getId() : null,
            ct.category() != null ? ct.category().getName() : "Sem Categoria",
            ct.totalValue()
        );
    }

    private TransactionDashboardDTO transactionToDashboardDTO(Transaction transaction) {
        return new TransactionDashboardDTO(
            transaction.getId(),
            transaction.getName(),
            transaction.getReleaseDate(),
            transaction.getValue(),
            transaction.getType() != null ? transaction.getType().toString() : null,
            transaction.getAccount().getAccountName(),
            transaction.getCategory() != null ? transaction.getCategory().getName() : null,
            transaction.getState() != null ? transaction.getState().toString() : null
        );
    }
    
    private Comparator<Transaction> obterComparador(TransactionOrder order) {
        TransactionOrder actualOrder = (order == null) ? TransactionOrder.DATA : order;
        switch (actualOrder) {
            case DATA:
                return Comparator.comparing(Transaction::getReleaseDate, Comparator.nullsLast(LocalDateTime::compareTo)).reversed();
            case CATEGORIA:
                return Comparator.comparing(t -> (t.getCategory() != null && t.getCategory().getName() != null) ? t.getCategory().getName() : "", String.CASE_INSENSITIVE_ORDER);
            case VALOR_CRESCENTE:
                return Comparator.comparing(Transaction::getValue);
            case VALOR_DECRESCENTE:
                return Comparator.comparing(Transaction::getValue).reversed();
            default:
                return Comparator.comparing(Transaction::getReleaseDate, Comparator.nullsLast(LocalDateTime::compareTo)).reversed();
        }
    }
}