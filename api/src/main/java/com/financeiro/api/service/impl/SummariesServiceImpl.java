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

                    dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(
                            null, 
                            "Saldo Consolidado Total", 
                            somaDosSaldosAtuaisConsolidados,
                            "Soma dos Saldos Atuais de Todas as Contas"
                    ));

                    if (incluirPrevisao) {
                        dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(
                                null, 
                                "Saldo Consolidado Total (Previsto)", 
                                somaDosSaldosPrevistosConsolidados,
                                "Soma dos Saldos Previstos de Todas as Contas"
                        ));
                    }
                } else { 
                    dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(
                            null, "Saldo Consolidado Total", 0.0, "Nenhuma conta ativa para consolidar"
                    ));
                    if (incluirPrevisao) {
                    dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(
                            null, "Saldo Consolidado Total (Previsto)", 0.0, "Nenhuma conta ativa para consolidar previsão"
                    ));
                    }
                }
            }
        }

        List<DashboardItemDTO> resultadosPrincipais = new ArrayList<>();

            TipoDado tipoDadoPrincipal = filtro.tipoDado() != null ? filtro.tipoDado() : TipoDado.TRANSACAO;

            if (tipoDadoPrincipal == TipoDado.CATEGORIA) {
                Stream<Transaction> transactionStreamBase = transactionRepository.findAll().stream()
                    .filter(t -> filtro.contaIds() == null || filtro.contaIds().isEmpty() || (t.getAccount() != null && filtro.contaIds().contains(t.getAccount().getId())))
                    .filter(t -> filtro.categoriaIds() == null || filtro.categoriaIds().isEmpty() || (t.getCategory() != null && filtro.categoriaIds().contains(t.getCategory().getId())))
                    .filter(t -> filtro.categoriaTipo() == null || (t.getCategory() != null && t.getCategory().getType() == filtro.categoriaTipo()))
                    .filter(t -> { if (filtro.transacaoTipo() == null) return true; if (filtro.transacaoTipo() == TransactionType.RECEITA || filtro.transacaoTipo() == TransactionType.DESPESA) return t.getType() == filtro.transacaoTipo(); return t.getTransferGroupId() != null; })
                    .filter(t -> filtro.estado() == null || t.getState() == filtro.estado())
                    .filter(t -> filtro.frequencia() == null || t.getFrequency() == filtro.frequencia())
                    .filter(t -> { LocalDateTime dataComparacao; DataReferencia dataReferencia = filtro.dataReferencia() != null ? filtro.dataReferencia() : DataReferencia.LANCAMENTO; if (dataReferencia == DataReferencia.LANCAMENTO) dataComparacao = t.getCreatedAt(); else dataComparacao = t.getReleaseDate(); if (dataComparacao == null) return false; return (filtro.dataInicio() == null || !dataComparacao.isBefore(filtro.dataInicio())) && (filtro.dataFim() == null || !dataComparacao.isAfter(filtro.dataFim())); });
                List<Transaction> filteredTransactionsForCategory = transactionStreamBase.toList();
                int limite = (filtro.limite() != null && filtro.limite() > 0) ? filtro.limite() : 20;
                Map<Category, Double> categorySums = filteredTransactionsForCategory.stream().filter(t -> t.getCategory() != null).collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getValue)));
                List<CategoryTotal> categoryTotalsList = categorySums.entrySet().stream().map(entry -> new CategoryTotal(entry.getKey(), entry.getValue())).collect(Collectors.toList());
                if (filtro.categoriaIds() == null || filtro.categoriaIds().isEmpty()) {
                    double uncategorizedSum = filteredTransactionsForCategory.stream().filter(t -> t.getCategory() == null).mapToDouble(Transaction::getValue).sum();
                    if (uncategorizedSum != 0) categoryTotalsList.add(new CategoryTotal(null, uncategorizedSum));
                }
                TipoApresentacaoDados apresentacaoCat = filtro.apresentacao() != null ? filtro.apresentacao() : TipoApresentacaoDados.TODOS;
                switch (apresentacaoCat) {
                    case PRIMEIROS: resultadosPrincipais.addAll(categoryTotalsList.stream().sorted(Comparator.comparing(CategoryTotal::totalValue).reversed()).limit(limite).map(this::categoryTotalToDashboardDTO).collect(Collectors.toList())); break;
                    case ULTIMOS: resultadosPrincipais.addAll(categoryTotalsList.stream().sorted(Comparator.comparing(CategoryTotal::totalValue)).limit(limite).map(this::categoryTotalToDashboardDTO).collect(Collectors.toList())); break;
                    case SOMA: double totalSumForCategories = filteredTransactionsForCategory.stream().mapToDouble(Transaction::getValue).sum(); resultadosPrincipais.add(new CategorySummaryDashboardDTO(null, "Soma Total Agrupada por Categoria", totalSumForCategories)); break;
                    case TODOS: default:
                        Comparator<CategoryTotal> todosComparator;
                        TransactionOrder orderCat = filtro.ordenacao();
                        if (orderCat == TransactionOrder.VALOR_CRESCENTE) todosComparator = Comparator.comparing(CategoryTotal::totalValue);
                        else if (orderCat == TransactionOrder.VALOR_DECRESCENTE) todosComparator = Comparator.comparing(CategoryTotal::totalValue).reversed();
                        else todosComparator = Comparator.comparing(ct -> (ct.category() != null && ct.category().getName() != null) ? ct.category().getName() : "Sem Categoria", String.CASE_INSENSITIVE_ORDER);
                        resultadosPrincipais.addAll(categoryTotalsList.stream().sorted(todosComparator).map(this::categoryTotalToDashboardDTO).collect(Collectors.toList())); break;
                }

            } else if (tipoDadoPrincipal == TipoDado.TRANSACAO) {
                Stream<Transaction> transactionStream = transactionRepository.findAll().stream()
                    .filter(t -> filtro.contaIds() == null || filtro.contaIds().isEmpty() || (t.getAccount() != null && filtro.contaIds().contains(t.getAccount().getId())))
                    .filter(t -> filtro.categoriaIds() == null || filtro.categoriaIds().isEmpty() || (t.getCategory() != null && filtro.categoriaIds().contains(t.getCategory().getId())))
                    .filter(t -> filtro.categoriaTipo() == null || (t.getCategory() != null && t.getCategory().getType() == filtro.categoriaTipo()))
                    .filter(t -> { if (filtro.transacaoTipo() == null) return true; if (filtro.transacaoTipo() == TransactionType.RECEITA || filtro.transacaoTipo() == TransactionType.DESPESA) return t.getType() == filtro.transacaoTipo(); return t.getTransferGroupId() != null; })
                    .filter(t -> filtro.estado() == null || t.getState() == filtro.estado())
                    .filter(t -> filtro.frequencia() == null || t.getFrequency() == filtro.frequencia())
                    .filter(t -> { LocalDateTime dataComparacao; DataReferencia dataReferencia = filtro.dataReferencia() != null ? filtro.dataReferencia() : DataReferencia.LANCAMENTO; if (dataReferencia == DataReferencia.LANCAMENTO) dataComparacao = t.getCreatedAt(); else dataComparacao = t.getReleaseDate(); if (dataComparacao == null) return false; return (filtro.dataInicio() == null || !dataComparacao.isBefore(filtro.dataInicio())) && (filtro.dataFim() == null || !dataComparacao.isAfter(filtro.dataFim())); });
                List<Transaction> filteredTransactions = transactionStream.toList();
                Comparator<Transaction> transactionComparator = obterComparador(filtro.ordenacao());
                List<Transaction> sortedTransactions = filteredTransactions.stream().sorted(transactionComparator).toList();
                int limite = (filtro.limite() != null && filtro.limite() > 0) ? filtro.limite() : 20;
                TipoApresentacaoDados apresentacao = filtro.apresentacao() != null ? filtro.apresentacao() : TipoApresentacaoDados.TODOS;
                switch (apresentacao) {
                    case PRIMEIROS: resultadosPrincipais.addAll(sortedTransactions.stream().limit(limite).map(this::transactionToDashboardDTO).collect(Collectors.toList())); break;
                    case ULTIMOS: resultadosPrincipais.addAll(filteredTransactions.stream().sorted(transactionComparator.reversed()).limit(limite).map(this::transactionToDashboardDTO).collect(Collectors.toList())); break;
                    case SOMA: double totalSumTransactions = sortedTransactions.stream().mapToDouble(Transaction::getValue).sum(); resultadosPrincipais.add(new CategorySummaryDashboardDTO(null, "Soma Total de Transações Filtradas", totalSumTransactions)); break;
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