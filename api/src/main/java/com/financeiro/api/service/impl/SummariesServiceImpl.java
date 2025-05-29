// Em com.financeiro.api.service.impl.SummaryServiceImpl.java
package com.financeiro.api.service.impl;

import com.financeiro.api.domain.*;
import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.dashboardDTO.AccountBalanceDashboardDTO; 
import com.financeiro.api.dto.dashboardDTO.CategorySummaryDashboardDTO; 
import com.financeiro.api.dto.dashboardDTO.DashboardItemDTO; 
import com.financeiro.api.dto.dashboardDTO.TransactionDashboardDTO; 
import com.financeiro.api.dto.transactionDTO.TransactionAdvancedFilterDTO;
import com.financeiro.api.repository.AccountRepository;
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

    public SummariesServiceImpl(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository) { 
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<DashboardItemDTO> generateSummary(TransactionAdvancedFilterDTO filtro) { // Tipo de retorno atualizado
        List<DashboardItemDTO> dtosDeSaldo = new ArrayList<>(); // Tipo atualizado
        boolean calcularSaldoComPrevisaoConformeDTO = Boolean.TRUE.equals(filtro.incluirSaldoPrevisto());

        // --- PASSO 1: Calcular e criar DTOs de Saldo ---
        if (filtro.contaIds() != null && !filtro.contaIds().isEmpty()) {
            for (UUID accountId : filtro.contaIds()) {
                Optional<Account> optAccount = accountRepository.findById(accountId);
                if (optAccount.isPresent()) {
                    Account account = optAccount.get();
                    if (account.getStatus() != Status.EXC) {
                        Double vSaldoInicial = account.getOpeningBalance() != null ? account.getOpeningBalance() : 0.0;
                        Double vReceitas = account.getIncome() != null ? account.getIncome() : 0.0;
                        Double vDespesas = account.getExpense() != null ? account.getExpense() : 0.0;
                        Double vChequeEspecial = account.getSpecialCheck() != null ? account.getSpecialCheck() : 0.0;
                        Double vReceitasPrevistas = account.getExpectedIncomeMonth() != null ? account.getExpectedIncomeMonth() : 0.0;
                        Double vDespesasPrevistas = account.getExpectedExpenseMonth() != null ? account.getExpectedExpenseMonth() : 0.0;
                        Double saldoAtualCalculado = vSaldoInicial + vReceitas - vDespesas;
                        Double saldoPrevistoCalculado = saldoAtualCalculado + vChequeEspecial + vReceitasPrevistas - vDespesasPrevistas;
                        Double saldoTotalCalculado = saldoPrevistoCalculado + vSaldoInicial;
                        Double saldoFinalParaRetorno;
                        String tipoSaldoStr;
                        if (calcularSaldoComPrevisaoConformeDTO) {
                            saldoFinalParaRetorno = saldoTotalCalculado;
                            tipoSaldoStr = "Saldo Total (Previsto + Inicial) da Conta: " + account.getAccountName();
                        } else {
                            saldoFinalParaRetorno = saldoAtualCalculado;
                            tipoSaldoStr = "Saldo Atual da Conta: " + account.getAccountName();
                        }
                        dtosDeSaldo.add(new AccountBalanceDashboardDTO(account.getId(), account.getAccountName(), saldoFinalParaRetorno, tipoSaldoStr));
                    }
                }
            }
        } else {
            List<Account> todasContasAtivas = accountRepository.findAllByStatusIn(List.of(Status.SIM, Status.NAO));
            double saldoAgregadoCalculado = 0.0;
            for (Account account : todasContasAtivas) {
                Double vSaldoInicial = account.getOpeningBalance() != null ? account.getOpeningBalance() : 0.0;
                Double vReceitas = account.getIncome() != null ? account.getIncome() : 0.0;
                Double vDespesas = account.getExpense() != null ? account.getExpense() : 0.0;
                Double vChequeEspecial = account.getSpecialCheck() != null ? account.getSpecialCheck() : 0.0;
                Double vReceitasPrevistas = account.getExpectedIncomeMonth() != null ? account.getExpectedIncomeMonth() : 0.0;
                Double vDespesasPrevistas = account.getExpectedExpenseMonth() != null ? account.getExpectedExpenseMonth() : 0.0;
                Double saldoAtualCalculado = vSaldoInicial + vReceitas - vDespesas;
                Double saldoPrevistoCalculado = saldoAtualCalculado + vChequeEspecial + vReceitasPrevistas - vDespesasPrevistas;
                Double saldoTotalCalculado = saldoPrevistoCalculado + vSaldoInicial;
                if (calcularSaldoComPrevisaoConformeDTO) saldoAgregadoCalculado += saldoTotalCalculado;
                else saldoAgregadoCalculado += saldoAtualCalculado;
            }
            String tipoSaldoAgregadoStr = calcularSaldoComPrevisaoConformeDTO ? "Soma dos Saldos Totais (Previsto + Inicial)" : "Soma dos Saldos Atuais";
            dtosDeSaldo.add(new AccountBalanceDashboardDTO(null, "Saldo Consolidado de Contas", saldoAgregadoCalculado, tipoSaldoAgregadoStr + " (todas as contas ativas)."));
        }

        List<DashboardItemDTO> resultadosPrincipais = new ArrayList<>(); 
        if (!Boolean.TRUE.equals(filtro.mostrarApenasSaldo())) {
            TipoDado tipoDadoPrincipal = filtro.tipoDado() != null ? filtro.tipoDado() : TipoDado.TRANSACAO;

            if (tipoDadoPrincipal == TipoDado.CATEGORIA) {
                
                 Stream<Transaction> transactionStreamBase = transactionRepository.findAll().stream()
                    .filter(t -> filtro.contaIds() == null || filtro.contaIds().isEmpty() || (t.getAccount() != null && filtro.contaIds().contains(t.getAccount().getId())))
                    .filter(t -> filtro.categoriaIds() == null || filtro.categoriaIds().isEmpty() || (t.getCategory() != null && filtro.categoriaIds().contains(t.getCategory().getId())))
                    .filter(t -> filtro.categoriaTipo() == null || (t.getCategory() != null && t.getCategory().getType() == filtro.categoriaTipo()))
                    .filter(t -> {
                                    if (filtro.transacaoTipo() == null) return true; 
                                    if (filtro.transacaoTipo() == TransactionType.RECEITA || filtro.transacaoTipo() == TransactionType.DESPESA) {
                                        return t.getType() == filtro.transacaoTipo(); 
                                    }
                                    return t.getTransferGroupId() != null;
                                })
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
                    .filter(t -> {
                                    if (filtro.transacaoTipo() == null) return true; 
                                    if (filtro.transacaoTipo() == TransactionType.RECEITA || filtro.transacaoTipo() == TransactionType.DESPESA) {
                                        return t.getType() == filtro.transacaoTipo(); 
                                    }
                                    return t.getTransferGroupId() != null;
                                })
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
                    case SOMA: double totalSumTransactions = sortedTransactions.stream().mapToDouble(Transaction::getValue).sum(); resultadosPrincipais.add(new CategorySummaryDashboardDTO(null, "Soma Total de Transações Filtradas", totalSumTransactions)); break; // Reutilizando CategorySummaryDTO para soma
                    case TODOS: default: resultadosPrincipais.addAll(sortedTransactions.stream().map(this::transactionToDashboardDTO).collect(Collectors.toList())); break;
                }
            }
        }

        List<DashboardItemDTO> respostaFinal = new ArrayList<>(dtosDeSaldo); 
        respostaFinal.addAll(resultadosPrincipais);
        return respostaFinal;
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