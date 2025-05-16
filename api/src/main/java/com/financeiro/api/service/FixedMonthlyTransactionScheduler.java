package com.financeiro.api.service;

import com.financeiro.api.domain.Transaction;
import com.financeiro.api.domain.enums.Frequency;
import com.financeiro.api.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
public class FixedMonthlyTransactionScheduler {

    private final TransactionRepository transactionRepository;

    public FixedMonthlyTransactionScheduler(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Scheduled(cron = "0 0 5 * * *") 
    @Transactional
    public void gerarTransacoesFixasMensais() {
        LocalDateTime hoje = LocalDateTime.now();

        List<Transaction> fixas = transactionRepository.findByFrequency(Frequency.FIXED_MONTHLY);

        for (Transaction original : fixas) {
            
            boolean jaExiste = transactionRepository.existsByNameAndAccountIdAndReleaseDateBetween(
                    original.getName(),
                    original.getAccount().getId(),
                    hoje.withDayOfMonth(1).with(LocalTime.MIN),
                    hoje.withDayOfMonth(hoje.toLocalDate().lengthOfMonth()).with(LocalTime.MAX)
            );

            if (jaExiste) continue;

            Transaction nova = new Transaction();
            nova.setName(original.getName());
            nova.setType(original.getType());
            nova.setStatus(original.getStatus());
            nova.setReleaseDate(primeiroDiaUtil(hoje));
            nova.setValue(original.getValue());
            nova.setDescription(original.getDescription());
            nova.setState(original.getState());
            nova.setAdditionalInformation(original.getAdditionalInformation());
            nova.setFrequency(original.getFrequency());
            nova.setAccount(original.getAccount());
            nova.setCategory(original.getCategory());
            nova.setSubcategory(original.getSubcategory());
            nova.setCreatedAt(LocalDateTime.now());
            nova.setUpdatedAt(LocalDateTime.now());

            transactionRepository.save(nova);
        }
    }

    private LocalDateTime primeiroDiaUtil(LocalDateTime base) {
        LocalDate data = base.withDayOfMonth(1).toLocalDate();
        while (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            data = data.plusDays(1);
        }
        return data.atTime(8, 0); 
    }
}
