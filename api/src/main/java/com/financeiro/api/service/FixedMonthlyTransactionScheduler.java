package com.financeiro.api.service;

import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.enums.Frequency;
import com.financeiro.api.domain.enums.Status;
import com.financeiro.api.domain.enums.TransactionState;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.service.impl.TransactionServiceImpl; // Importar a implementação ou a interface TransactionService
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Usar a anotação do Spring

import java.time.*;
import java.util.List;

@Component
public class FixedMonthlyTransactionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(FixedMonthlyTransactionScheduler.class);

    private final TransactionRepository transactionRepository;
    private final TransactionServiceImpl transactionService; // Injetar o TransactionService

    // Atualizar o construtor
    public FixedMonthlyTransactionScheduler(TransactionRepository transactionRepository,
                                            TransactionServiceImpl transactionService) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    @Scheduled(cron = "0 0 5 * * *") // Sua tarefa existente: Roda às 5 da manhã
    @Transactional
    public void gerarTransacoesFixasMensais() {
        logger.info("Iniciando tarefa agendada: gerarTransacoesFixasMensais - {}", LocalDateTime.now());
        LocalDateTime hoje = LocalDateTime.now();
        List<Transaction> fixas = transactionRepository.findByFrequency(Frequency.FIXED_MONTHLY);

        for (Transaction original : fixas) {
            boolean jaExiste = transactionRepository.existsByNameAndAccountIdAndReleaseDateBetween(
                    original.getName(),
                    original.getAccount().getId(),
                    hoje.withDayOfMonth(1).with(LocalTime.MIN),
                    hoje.withDayOfMonth(hoje.toLocalDate().lengthOfMonth()).with(LocalTime.MAX)
            );

            if (jaExiste) {
                logger.debug("Transação fixa mensal já existe para {} na conta {} este mês.", original.getName(), original.getAccount().getAccountName());
                continue;
            }

            Transaction nova = new Transaction();
            nova.setName(original.getName());
            nova.setType(original.getType());
            nova.setStatus(original.getStatus());
            nova.setReleaseDate(primeiroDiaUtil(hoje));
            nova.setValue(original.getValue());
            nova.setDescription(original.getDescription());
            nova.setState(original.getState()); // Ex: PENDING ou EFFECTIVE, dependendo da sua regra de negócio para novas fixas
            nova.setAdditionalInformation(original.getAdditionalInformation());
            nova.setFrequency(original.getFrequency());
            nova.setAccount(original.getAccount());
            nova.setCategory(original.getCategory());
            nova.setSubcategory(original.getSubcategory());
            nova.setUser(original.getUser()); // Propagar o usuário
            nova.setCreatedAt(LocalDateTime.now());
            nova.setUpdatedAt(LocalDateTime.now());

            logger.info("Gerando nova transação fixa mensal: {} para conta {}", nova.getName(), nova.getAccount().getAccountName());
            
            // IMPORTANTE: Se esta nova transação deve impactar saldos da conta imediatamente,
            // você deveria chamar o `transactionService.create(DTO)` aqui, em vez de `transactionRepository.save()`.
            // Se ela for criada como PENDING e a `releaseDate` for hoje ou no passado,
            // a tarefa `processPendingTransactions` (abaixo) a pegará.
            transactionRepository.save(nova); 
        }
        logger.info("Tarefa agendada: gerarTransacoesFixasMensais finalizada - {}", LocalDateTime.now());
    }

    private LocalDateTime primeiroDiaUtil(LocalDateTime base) {
        LocalDate data = base.withDayOfMonth(1).toLocalDate();
        while (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            data = data.plusDays(1);
        }
        return data.atTime(8, 0);
    }

    // NOVO MÉTODO AGENDADO PARA EFETIVAR TRANSAÇÕES PENDENTES
    @Scheduled(cron = "0 10 1 * * *") // Exemplo: todo dia à 01:10:00 da manhã (ajuste o cron conforme necessário)
    @Transactional
    public void processPendingTransactions() {
        logger.info("Iniciando tarefa agendada: processPendingTransactions - {}", LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();

        List<Transaction> transactionsToProcess = transactionRepository.findAllByStateAndStatusAndReleaseDateLessThanEqual(
            TransactionState.PENDING,
            Status.SIM, // Apenas transações ativas (não Status.EXC, por exemplo)
            now
        );

        if (transactionsToProcess.isEmpty()) {
            logger.info("Nenhuma transação pendente para processar.");
            return;
        }

        logger.info("Encontradas {} transações pendentes para processar.", transactionsToProcess.size());

        for (Transaction transaction : transactionsToProcess) {
            try {
                logger.info("Processando transação ID: {}, ReleaseDate: {}", transaction.getId(), transaction.getReleaseDate());
                // Chama o método updateState do TransactionService.
                // Este método DEVE estar implementado para:
                // 1. Mudar o estado da transação para EFFECTIVE.
                // 2. Ajustar os saldos da conta (diminuir dos previstos, aumentar nos efetivos).
                // 3. Recalcular currentBalance e expectedBalance da conta.
                // 4. Ser @Transactional.
                transactionService.updateState(transaction.getId(), TransactionState.EFFECTIVE);
                logger.info("Transação ID: {} efetivada com sucesso.", transaction.getId());
            } catch (Exception e) {
                logger.error("Erro ao processar transação pendente ID: {}. Causa: {}", transaction.getId(), e.getMessage(), e);
                // Continuar com as próximas transações para não parar todo o processo.
            }
        }
        logger.info("Tarefa agendada: processPendingTransactions finalizada - {}", LocalDateTime.now());
    }
}