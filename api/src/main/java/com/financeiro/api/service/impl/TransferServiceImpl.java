package com.financeiro.api.service.impl;

import com.financeiro.api.domain.*;
import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.transactionDTO.TransactionResponseDTO;
import com.financeiro.api.dto.transferDTO.TransferRequestDTO;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.service.TransferService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication; // Importar
import org.springframework.security.core.context.SecurityContextHolder; // Importar
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // IMPORTANTE

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransferServiceImpl implements TransferService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransferServiceImpl(TransactionRepository transactionRepository,
                               AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new com.financeiro.api.infra.exceptions.UserNotFoundException("Usuário não autenticado ou não encontrado na sessão.");
        }
        return (User) authentication.getPrincipal();
    }


    @Override
    @Transactional
    public List<TransactionResponseDTO> transfer(TransferRequestDTO dto) {
        User currentUser = getCurrentUser(); 

        Account origin = accountRepository.findById(dto.originAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Conta de origem não encontrada com ID: " + dto.originAccountId()));

        Account destination = accountRepository.findById(dto.destinationAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Conta de destino não encontrada com ID: " + dto.destinationAccountId()));

        if (origin.getId().equals(destination.getId())) {
            throw new IllegalArgumentException("A conta de origem e destino não podem ser a mesma.");
        }

        int totalInstallments = dto.frequency() == Frequency.REPEAT && dto.installments() != null && dto.installments() > 0 ? dto.installments() : 1;

        double valorParcela = dto.value() / totalInstallments; 
        UUID recurringGroupId = (dto.frequency() == Frequency.REPEAT && totalInstallments > 1) ? UUID.randomUUID() : null;
        UUID transferGroupId = UUID.randomUUID(); 
        
        List<TransactionResponseDTO> responses = new ArrayList<>();

        for (int i = 0; i < totalInstallments; i++) {
            LocalDateTime releaseDate = dto.releaseDate();

            if (dto.frequency() == Frequency.REPEAT && dto.periodicity() != null && totalInstallments > 1) {
                releaseDate = calcularDataParcela(dto.releaseDate(), dto.periodicity(), i, dto.businessDayOnly());
            }

            Integer parcelaAtual = (dto.frequency() == Frequency.REPEAT && totalInstallments > 1) ? i + 1 : null;

            Transaction out = new Transaction();
            out.setAccount(origin);
            out.setType(TransactionType.DESPESA);
            out.setName(dto.name() + (totalInstallments > 1 ? " (Saída Transf. Parcela " + parcelaAtual + "/" + totalInstallments + ")" : " (Saída Transf.)"));
            out.setDescription(dto.description());
            out.setStatus(Status.SIM); 
            out.setState(dto.state()); 
            out.setAdditionalInformation(dto.additionalInformation());
            out.setReleaseDate(releaseDate);
            out.setValue(valorParcela);
            out.setFrequency(dto.frequency());
            out.setInstallments(dto.installments());
            out.setPeriodicity(dto.periodicity());
            out.setBusinessDayOnly(dto.businessDayOnly());
            out.setInstallmentNumber(parcelaAtual);
            out.setRecurringGroupId(recurringGroupId);
            out.setTransferGroupId(transferGroupId);
            out.setUser(currentUser); 
            out.setCreatedAt(LocalDateTime.now());
            out.setUpdatedAt(LocalDateTime.now());
            Transaction savedOut = transactionRepository.save(out);

            Transaction in = new Transaction();
            in.setAccount(destination);
            in.setType(TransactionType.RECEITA);
            in.setName(dto.name() + (totalInstallments > 1 ? " (Entrada Transf. Parcela " + parcelaAtual + "/" + totalInstallments + ")" : " (Entrada Transf.)"));
            in.setDescription(dto.description());
            in.setStatus(Status.SIM);
            in.setState(dto.state());
            in.setAdditionalInformation(dto.additionalInformation());
            in.setReleaseDate(releaseDate);
            in.setValue(valorParcela);
            in.setFrequency(dto.frequency());
            in.setInstallments(dto.installments());
            in.setPeriodicity(dto.periodicity());
            in.setBusinessDayOnly(dto.businessDayOnly());
            in.setInstallmentNumber(parcelaAtual);
            in.setRecurringGroupId(recurringGroupId);
            in.setTransferGroupId(transferGroupId);
            in.setUser(currentUser); 
            in.setCreatedAt(LocalDateTime.now());
            in.setUpdatedAt(LocalDateTime.now());
            Transaction savedIn = transactionRepository.save(in);

            initializeAccountBalances(origin); 
            if (dto.state() == TransactionState.EFFECTIVE) {
                origin.setExpense(origin.getExpense() + valorParcela);
            } else if (dto.state() == TransactionState.PENDING) {
                origin.setExpectedExpenseMonth(origin.getExpectedExpenseMonth() + valorParcela);
            }
            recalculateAccountBalances(origin);
            origin.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(origin);

            initializeAccountBalances(destination); 
            if (dto.state() == TransactionState.EFFECTIVE) {
                destination.setIncome(destination.getIncome() + valorParcela);
            } else if (dto.state() == TransactionState.PENDING) {
                destination.setExpectedIncomeMonth(destination.getExpectedIncomeMonth() + valorParcela);
            }
            recalculateAccountBalances(destination);
            destination.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(destination);
            
             boolean saldoNegativoAposTransferencia = origin.getCurrentBalance() < 0;


            responses.add(toDTO(savedOut, saldoNegativoAposTransferencia && dto.state() == TransactionState.EFFECTIVE));
            responses.add(toDTO(savedIn, false)); 
        }

        return responses;
    }

    private void initializeAccountBalances(Account account) {
        if (account.getOpeningBalance() == null) account.setOpeningBalance(0.0);
        if (account.getCurrentBalance() == null) account.setCurrentBalance(account.getOpeningBalance());
        if (account.getIncome() == null) account.setIncome(0.0);
        if (account.getExpense() == null) account.setExpense(0.0);
        if (account.getExpectedIncomeMonth() == null) account.setExpectedIncomeMonth(0.0);
        if (account.getExpectedExpenseMonth() == null) account.setExpectedExpenseMonth(0.0);
        if (account.getSpecialCheck() == null) account.setSpecialCheck(0.0);
        if (account.getExpectedBalance() == null) { 
             recalculateAccountBalances(account);
        }
    }

    private void recalculateAccountBalances(Account account) {

        account.setCurrentBalance(
            (account.getOpeningBalance() != null ? account.getOpeningBalance() : 0.0) +
            (account.getIncome() != null ? account.getIncome() : 0.0) -
            (account.getExpense() != null ? account.getExpense() : 0.0)
        );

        account.setExpectedBalance(
            (account.getCurrentBalance() != null ? account.getCurrentBalance() : 0.0) + 
            (account.getSpecialCheck() != null ? account.getSpecialCheck() : 0.0) +
            (account.getExpectedIncomeMonth() != null ? account.getExpectedIncomeMonth() : 0.0) -
            (account.getExpectedExpenseMonth() != null ? account.getExpectedExpenseMonth() : 0.0)
        );
    }

    private LocalDateTime calcularDataParcela(LocalDateTime base, Periodicity periodicity, int index, Boolean diasUteis) {
        LocalDate data = base.toLocalDate();
        data = switch (periodicity) {
            case DIARIO -> data.plusDays(index);
            case SEMANAL -> data.plusWeeks(index);
            case QUINZENAL -> data.plusWeeks(index * 2L);
            case MENSAL -> data.plusMonths(index);
            case TRIMESTRAL -> data.plusMonths(index * 3L);
            case SEMESTRAL -> data.plusMonths(index * 6L);
            case ANUAL -> data.plusYears(index);
        };
        if (Boolean.TRUE.equals(diasUteis)) {
            while (data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY) {
                data = data.plusDays(1);
            }
        }
        return base.withYear(data.getYear())
                .withMonth(data.getMonthValue())
                .withDayOfMonth(data.getDayOfMonth())
                .withHour(base.getHour()) 
                .withMinute(base.getMinute())
                .withSecond(base.getSecond())
                .withNano(base.getNano());
    }

    private TransactionResponseDTO toDTO(Transaction t, boolean saldoNegativo) {

        UUID subcategoryId = t.getSubcategory() != null ? t.getSubcategory().getId() : null;
        String subcategoryName = t.getSubcategory() != null ? t.getSubcategory().getName() : null;

        return new TransactionResponseDTO(
                t.getId(),
                t.getAccount().getId(),
                t.getAccount().getAccountName(),
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getCategory() != null ? t.getCategory().getName() : null,
                subcategoryId, 
                subcategoryName, 
                t.getName(),
                t.getType(),
                t.getStatus(),
                t.getReleaseDate(),
                t.getValue(),
                t.getDescription(),
                t.getState(),
                t.getAdditionalInformation(),
                t.getFrequency(),
                t.getInstallments(),
                t.getPeriodicity(),
                t.getBusinessDayOnly(),
                t.getInstallmentNumber(),
                t.getRecurringGroupId(),
                t.getTransferGroupId(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                saldoNegativo
        );
    }
}