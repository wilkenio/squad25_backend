package com.financeiro.api.service.scheduler;

import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.enums.Frequency;
import com.financeiro.api.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RecurringTransactionScheduler {

    private final TransactionRepository repository;

    public RecurringTransactionScheduler(TransactionRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 6 * * ?") 
    @Transactional
    public void replicateFixedMonthlyTransactions() {
        List<Transaction> recurringTransactions = repository.findAll().stream()
                .filter(t -> t.getFrequency() == Frequency.MONTHLY)
                .filter(t -> t.getReleaseDate().getDayOfMonth() == LocalDateTime.now().getDayOfMonth())
                .toList();

        for (Transaction t : recurringTransactions) {
            Transaction nova = new Transaction();
            nova.setAccount(t.getAccount());
            nova.setCategory(t.getCategory());
            nova.setSubcategory(t.getSubcategory());
            nova.setName(t.getName());
            nova.setType(t.getType());
            nova.setStatus(t.getStatus());
            nova.setValue(t.getValue());
            nova.setDescription(t.getDescription());
            nova.setState(t.getState());
            nova.setAdditionalInformation(t.getAdditionalInformation());
            nova.setFrequency(t.getFrequency());
            nova.setInstallments(t.getInstallments());
            nova.setReleaseDate(LocalDateTime.now());
            nova.setCreatedAt(LocalDateTime.now());
            nova.setUpdatedAt(LocalDateTime.now());
            repository.save(nova);
        }

        System.out.println("✔️ Lançamentos mensais replicados automaticamente.");
    }
}
