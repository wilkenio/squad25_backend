package com.financeiro.api.service.impl;

import com.financeiro.api.domain.Account;
import com.financeiro.api.domain.Category;
import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.dashboardDTO.AccountBalanceDashboardDTO;
import com.financeiro.api.dto.dashboardDTO.CategorySummaryDashboardDTO;
import com.financeiro.api.dto.dashboardDTO.DashboardItemDTO;
import com.financeiro.api.dto.dashboardDTO.TransactionDashboardDTO;
import com.financeiro.api.dto.transactionDTO.TransactionAdvancedFilterDTO;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.repository.UserRepository;
import com.financeiro.api.service.SummariesService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final UserRepository userRepository;

    public SummariesServiceImpl(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
            (authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal().toString()))) {
            throw new UsernameNotFoundException("Nenhum usuário autenticado válido encontrado para esta operação.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        } else if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário com email '" + username + "' não encontrado no repositório."));
        } else {
            throw new IllegalStateException("Tipo de Principal inesperado: " + principal.getClass().getName() +
                                            ". Verifique a configuração de segurança ou adapte o método getCurrentUser.");
        }
    }

    @Override
    public Page<DashboardItemDTO> generateSummary(TransactionAdvancedFilterDTO filtro) {
        User currentUser = this.getCurrentUser();

        List<DashboardItemDTO> dtosDeSaldoCalculados = new ArrayList<>();
        Set<UUID> userActiveAccountIds = accountRepository.findByUserAndStatusIn(currentUser, List.of(Status.SIM))
                                           .stream()
                                           .map(Account::getId)
                                           .collect(Collectors.toSet());

        if (Boolean.TRUE.equals(filtro.mostrarApenasSaldo())) {
            boolean incluirPrevisao = Boolean.TRUE.equals(filtro.incluirSaldoPrevisto());
            if (filtro.contaIds() != null && !filtro.contaIds().isEmpty()) {
                List<Account> accountsParaSaldo = new ArrayList<>();
                for (UUID accountId : filtro.contaIds()) {
                    Optional<Account> optAccount = accountRepository.findById(accountId);
                    if (optAccount.isPresent()) {
                        Account account = optAccount.get();
                        if (account.getUser() != null && currentUser.getId().equals(account.getUser().getId()) &&
                            account.getStatus() == Status.SIM) {
                            accountsParaSaldo.add(account);
                        }
                    }
                }
                for (Account account : accountsParaSaldo) {
                    Double vSaldoInicial = Optional.ofNullable(account.getOpeningBalance()).orElse(0.0);
                    Double vReceitas = Optional.ofNullable(account.getIncome()).orElse(0.0);
                    Double vDespesas = Optional.ofNullable(account.getExpense()).orElse(0.0);
                    Double vChequeEspecial = Optional.ofNullable(account.getSpecialCheck()).orElse(0.0);
                    Double vReceitasPrevistas = Optional.ofNullable(account.getExpectedIncomeMonth()).orElse(0.0);
                    Double vDespesasPrevistas = Optional.ofNullable(account.getExpectedExpenseMonth()).orElse(0.0);
                    Double saldoAtualCalculado = vSaldoInicial + vReceitas - vDespesas;
                    dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(account.getId(), account.getAccountName(), saldoAtualCalculado, "Saldo Atual"));
                    if (incluirPrevisao) {
                        Double saldoPrevistoCalculado = saldoAtualCalculado + vChequeEspecial + vReceitasPrevistas - vDespesasPrevistas;
                        dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(account.getId(), account.getAccountName(), saldoPrevistoCalculado, "Saldo Previsto"));
                    }
                }
            } else { 
                List<Account> todasContasAtivasDoUsuario = accountRepository.findByUserAndStatusIn(currentUser, List.of(Status.SIM));
                double somaDosSaldosAtuaisConsolidados = 0.0;
                double somaDosSaldosPrevistosConsolidados = 0.0;
                if (!todasContasAtivasDoUsuario.isEmpty()) {
                    for (Account account : todasContasAtivasDoUsuario) {
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
                    dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(null, "Saldo Consolidado Total", somaDosSaldosAtuaisConsolidados, "Soma dos Saldos Atuais de Suas Contas Ativas"));
                    if (incluirPrevisao) {
                        dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(null, "Saldo Consolidado Total (Previsto)", somaDosSaldosPrevistosConsolidados, "Soma dos Saldos Previstos de Suas Contas Ativas"));
                    }
                } else { 
                    dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(null, "Saldo Consolidado Total", 0.0, "Nenhuma conta ativa (Status SIM) para consolidar"));
                    if (incluirPrevisao) {
                        dtosDeSaldoCalculados.add(new AccountBalanceDashboardDTO(null, "Saldo Consolidado Total (Previsto)", 0.0, "Nenhuma conta ativa (Status SIM) para consolidar previsão"));
                    }
                }
            }
        }

        List<DashboardItemDTO> resultadosPrincipais = new ArrayList<>();
        TipoDado tipoDadoPrincipal = filtro.tipoDado() != null ? filtro.tipoDado() : TipoDado.TRANSACAO;

        final Set<UUID> finalAccountIdsForTransactionFilter;
        if (filtro.contaIds() != null && !filtro.contaIds().isEmpty()) {
            finalAccountIdsForTransactionFilter = filtro.contaIds().stream()
                                                        .filter(userActiveAccountIds::contains)
                                                        .collect(Collectors.toSet());
        } else {
            finalAccountIdsForTransactionFilter = userActiveAccountIds;
        }
        
        if (!finalAccountIdsForTransactionFilter.isEmpty()) {
            final int DEFAULT_LIMIT_FOR_PRESENTATION = 20; // Default para LISTA_LIMITADA

            Stream<Transaction> transactionStreamBase = transactionRepository.findAll().stream()
                    .filter(t -> t.getUser() != null && currentUser.getId().equals(t.getUser().getId()))
                    .filter(t -> t.getStatus() == Status.SIM) 
                    .filter(t -> t.getAccount() != null && finalAccountIdsForTransactionFilter.contains(t.getAccount().getId())) 
                    .filter(transaction -> { // Filtro de Categoria
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
                    .filter(transaction -> { // Filtro de Frequência
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
                    .filter(t -> { // Filtro de Data
                        LocalDateTime dataComparacao;
                        TransactionOrder ordenacaoAtual = filtro.ordenacao();
                        if (ordenacaoAtual == TransactionOrder.DATA_LANCAMENTO) {
                            dataComparacao = t.getCreatedAt();
                        } else {
                            dataComparacao = t.getReleaseDate();
                        }
                        if (dataComparacao == null) return false;
                        return (filtro.dataInicio() == null || !dataComparacao.isBefore(filtro.dataInicio())) &&
                               (filtro.dataFim() == null || !dataComparacao.isAfter(filtro.dataFim()));
                    })
                    .filter(transaction -> { // Filtro de Tipo/Estado da Transação
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
                int presentationLimit = (filtro.limite() != null && filtro.limite() > 0) ? filtro.limite() : DEFAULT_LIMIT_FOR_PRESENTATION;
                
                Map<Category, Double> categorySums = filteredTransactionsForCategory.stream()
                        .filter(t -> t.getCategory() != null) 
                        .collect(Collectors.groupingBy(Transaction::getCategory, Collectors.summingDouble(Transaction::getValue)));
                List<CategoryTotal> categoryTotalsList = categorySums.entrySet().stream()
                        .map(entry -> new CategoryTotal(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());

                boolean cReceitaAtivado = Boolean.TRUE.equals(filtro.incluirTodasCategoriasReceita()) || (filtro.idsCategoriasReceita() != null && !filtro.idsCategoriasReceita().isEmpty());
                boolean cDespesaAtivado = Boolean.TRUE.equals(filtro.incluirTodasCategoriasDespesa()) || (filtro.idsCategoriasDespesa() != null && !filtro.idsCategoriasDespesa().isEmpty());

                if (!cReceitaAtivado && !cDespesaAtivado) {
                    TransactionOrder ordenacaoFiltroUncat = filtro.ordenacao();
                    Stream<Transaction> streamForUncategorized = transactionRepository.findAll().stream()
                        .filter(t -> t.getUser() != null && currentUser.getId().equals(t.getUser().getId()))
                        .filter(t -> t.getStatus() == Status.SIM)
                        .filter(t -> t.getAccount() != null && finalAccountIdsForTransactionFilter.contains(t.getAccount().getId()))
                        .filter(t -> { 
                            LocalDateTime dataComparacaoUncat;
                            if (ordenacaoFiltroUncat == TransactionOrder.DATA_LANCAMENTO || ordenacaoFiltroUncat == TransactionOrder.DATA_LANCAMENTO) {
                                dataComparacaoUncat = t.getCreatedAt();
                            } else { dataComparacaoUncat = t.getReleaseDate(); }
                            return dataComparacaoUncat != null && (filtro.dataInicio() == null || !dataComparacaoUncat.isBefore(filtro.dataInicio())) && (filtro.dataFim() == null || !dataComparacaoUncat.isAfter(filtro.dataFim()));
                        }).filter(transaction -> { 
                            boolean isEfetivada = transaction.getState() == TransactionState.EFFECTIVE; boolean isPrevista = transaction.getState() == TransactionState.PENDING; boolean isActualTransfer = transaction.getTransferGroupId() != null;
                            boolean isReceitaType = transaction.getType() == TransactionType.RECEITA; boolean isDespesaType = transaction.getType() == TransactionType.DESPESA;
                            boolean anyPrimaryTypeFilterActive = Boolean.TRUE.equals(filtro.incluirReceitas()) || Boolean.TRUE.equals(filtro.incluirDespesas()) || Boolean.TRUE.equals(filtro.incluirTransferencias());
                            if (!anyPrimaryTypeFilterActive) return true;
                            if (Boolean.TRUE.equals(filtro.incluirReceitas()) && isReceitaType && !isActualTransfer) { boolean cE = Boolean.TRUE.equals(filtro.incluirReceitasEfetivadas()); boolean cP = Boolean.TRUE.equals(filtro.incluirReceitasPrevistas()); if (!cE && !cP) return true; if (cE && isEfetivada) return true; if (cP && isPrevista) return true; }
                            if (Boolean.TRUE.equals(filtro.incluirDespesas()) && isDespesaType && !isActualTransfer) { boolean cE = Boolean.TRUE.equals(filtro.incluirDespesasEfetivadas()); boolean cP = Boolean.TRUE.equals(filtro.incluirDespesasPrevistas()); if (!cE && !cP) return true; if (cE && isEfetivada) return true; if (cP && isPrevista) return true; }
                            if (Boolean.TRUE.equals(filtro.incluirTransferencias()) && isActualTransfer) { boolean cE = Boolean.TRUE.equals(filtro.incluirTransferenciasEfetivadas()); boolean cP = Boolean.TRUE.equals(filtro.incluirTransferenciasPrevistas()); if (!cE && !cP) return true; if (cE && isEfetivada) return true; if (cP && isPrevista) return true; }
                            return false;
                        }).filter(transaction -> { 
                            Frequency freq = transaction.getFrequency(); Periodicity period = transaction.getPeriodicity();
                            boolean naoRec = freq == Frequency.NON_RECURRING; boolean rep = freq == Frequency.REPEAT; boolean fixaM = rep && period == Periodicity.MENSAL;
                            boolean chkNR = Boolean.TRUE.equals(filtro.incluirFreqNaoRecorrente()); boolean chkR = Boolean.TRUE.equals(filtro.incluirFreqRepetida()); boolean chkFM = Boolean.TRUE.equals(filtro.incluirFreqFixaMensal());
                            if (!chkNR && !chkR && !chkFM) return true; if (chkNR && naoRec) return true; if (chkR && rep) return true; if (chkFM && fixaM) return true; return false;
                        });
                    double uncategorizedSum = streamForUncategorized.filter(t -> t.getCategory() == null).mapToDouble(Transaction::getValue).sum();
                    if (uncategorizedSum != 0) {
                        categoryTotalsList.add(new CategoryTotal(null, uncategorizedSum));
                    }
                }
                
                TransactionOrder orderCat = filtro.ordenacao();
                Comparator<CategoryTotal> categoryComparator;
                if (orderCat == TransactionOrder.VALOR_CRESCENTE) categoryComparator = Comparator.comparing(CategoryTotal::totalValue);
                else if (orderCat == TransactionOrder.VALOR_DECRESCENTE) categoryComparator = Comparator.comparing(CategoryTotal::totalValue).reversed();
                else categoryComparator = Comparator.comparing(ct -> (ct.category() != null && ct.category().getName() != null) ? ct.category().getName() : "Sem Categoria", String.CASE_INSENSITIVE_ORDER);
                List<CategoryTotal> sortedCategoryTotals = categoryTotalsList.stream().sorted(categoryComparator).toList();

                TipoApresentacaoDados apresentacaoCat = filtro.apresentacao();
                if (apresentacaoCat == TipoApresentacaoDados.LISTA_LIMITADA) {
                    resultadosPrincipais.addAll(sortedCategoryTotals.stream().limit(presentationLimit).map(this::categoryTotalToDashboardDTO).collect(Collectors.toList()));
                } else if (apresentacaoCat == TipoApresentacaoDados.SOMA) {
                    double totalSumForCategories = filteredTransactionsForCategory.stream().mapToDouble(Transaction::getValue).sum(); 
                    if (!cReceitaAtivado && !cDespesaAtivado) {
                         totalSumForCategories += categoryTotalsList.stream().filter(ct -> ct.category() == null).mapToDouble(CategoryTotal::totalValue).findFirst().orElse(0.0);
                    }
                    resultadosPrincipais.add(new CategorySummaryDashboardDTO(null, "Soma Total (Agrupado por Categoria)", totalSumForCategories)); 
                } else { 
                    resultadosPrincipais.addAll(sortedCategoryTotals.stream().map(this::categoryTotalToDashboardDTO).collect(Collectors.toList()));
                }

            } else if (tipoDadoPrincipal == TipoDado.TRANSACAO) {
                List<Transaction> filteredTransactions = transactionStreamBase.toList();
                Comparator<Transaction> transactionComparator = obterComparador(filtro.ordenacao());
                List<Transaction> sortedTransactions = filteredTransactions.stream().sorted(transactionComparator).toList();
                int presentationLimit = (filtro.limite() != null && filtro.limite() > 0) ? filtro.limite() : DEFAULT_LIMIT_FOR_PRESENTATION;
                TipoApresentacaoDados apresentacao = filtro.apresentacao();

                if (apresentacao == TipoApresentacaoDados.LISTA_LIMITADA) {
                    resultadosPrincipais.addAll(sortedTransactions.stream().limit(presentationLimit).map(this::transactionToDashboardDTO).collect(Collectors.toList()));
                } else if (apresentacao == TipoApresentacaoDados.SOMA) {
                    double totalSumTransactions = sortedTransactions.stream().mapToDouble(Transaction::getValue).sum(); 
                    resultadosPrincipais.add(new CategorySummaryDashboardDTO(null, "Soma Total (Agrupado por Transação)", totalSumTransactions)); 
                } else { 
                    resultadosPrincipais.addAll(sortedTransactions.stream().map(this::transactionToDashboardDTO).collect(Collectors.toList()));
                }
            }
        }

        List<DashboardItemDTO> respostaSemPaginacao = new ArrayList<>();
        respostaSemPaginacao.addAll(dtosDeSaldoCalculados); 
        respostaSemPaginacao.addAll(resultadosPrincipais);

        int pageNumber = (filtro.pageNumber() == null || filtro.pageNumber() < 0) ? 0 : filtro.pageNumber();
        int pageSize = (filtro.pageSize() == null || filtro.pageSize() <= 0) ? 20 : filtro.pageSize(); 
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        int totalItems = respostaSemPaginacao.size();
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), totalItems);
        List<DashboardItemDTO> paginatedList = (startIndex >= totalItems) ? Collections.emptyList() : respostaSemPaginacao.subList(startIndex, endIndex);

        return new PageImpl<>(paginatedList, pageable, totalItems);
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
        TransactionOrder actualOrder = (order == null) ? TransactionOrder.DATA_EFETIVACAO : order;
        switch (actualOrder) {
            case DATA_EFETIVACAO:
                return Comparator.comparing(Transaction::getReleaseDate, Comparator.nullsLast(LocalDateTime::compareTo)).reversed();
            case DATA_LANCAMENTO:
                return Comparator.comparing(Transaction::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
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