package com.financeiro.api.service.impl;

import com.financeiro.api.domain.*;
import com.financeiro.api.domain.enums.*;
import com.financeiro.api.dto.transactionDTO.TransactionResponseDTO;
import com.financeiro.api.dto.transferDTO.TransferRequestDTO;
import com.financeiro.api.repository.AccountRepository;
import com.financeiro.api.repository.CategoryRepository;
import com.financeiro.api.repository.TransactionRepository;
import com.financeiro.api.service.TransferService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransferServiceImpl implements TransferService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransferServiceImpl(TransactionRepository transactionRepository,
                               AccountRepository accountRepository,
                               CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<TransactionResponseDTO> transfer(TransferRequestDTO dto) {
        Account origin = accountRepository.findById(dto.originAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Conta de origem não encontrada"));

        Account destination = accountRepository.findById(dto.destinationAccountId())
                .orElseThrow(() -> new EntityNotFoundException("Conta de destino não encontrada"));

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        int total = dto.frequency() == Frequency.REPEAT ? dto.installments() : 1;
        double valorParcela = dto.value() / total;
        UUID groupId = UUID.randomUUID();
        List<TransactionResponseDTO> responses = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            LocalDateTime releaseDate = dto.releaseDate();

            if (dto.frequency() == Frequency.REPEAT && dto.periodicity() != null) {
                releaseDate = calcularDataParcela(dto.releaseDate(), dto.periodicity(), i, dto.businessDayOnly());
            }

            // ✅ Proteção contra saldo null
            Double saldoAtual = origin.getCurrentBalance() != null ? origin.getCurrentBalance() : 0.0;

            boolean saldoNegativo = dto.state() == TransactionState.EFFECTIVE
                    && valorParcela > saldoAtual;

            Integer parcelaAtual = dto.frequency() == Frequency.REPEAT ? i + 1 : null;

            // Transação de saída (DESPESA)
            Transaction out = new Transaction();
            out.setAccount(origin);
            out.setCategory(category);
            out.setType(TransactionType.DESPESA);
            out.setName(dto.name());
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
            out.setRecurringGroupId(groupId);
            out.setCreatedAt(LocalDateTime.now());
            out.setUpdatedAt(LocalDateTime.now());

            // Transação de entrada (RECEITA)
            Transaction in = new Transaction();
            in.setAccount(destination);
            in.setCategory(category);
            in.setType(TransactionType.RECEITA);
            in.setName(dto.name());
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
            in.setRecurringGroupId(groupId);
            in.setCreatedAt(LocalDateTime.now());
            in.setUpdatedAt(LocalDateTime.now());

            Transaction savedOut = transactionRepository.save(out);
            Transaction savedIn = transactionRepository.save(in);

            responses.add(toDTO(savedOut, saldoNegativo));
            responses.add(toDTO(savedIn, false));
        }

        return responses;
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
                .withDayOfMonth(data.getDayOfMonth());
    }

    private TransactionResponseDTO toDTO(Transaction t, boolean saldoNegativo) {
        return new TransactionResponseDTO(
                t.getId(),
                t.getAccount().getId(),
                t.getAccount().getAccountName(),
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getCategory() != null ? t.getCategory().getName() : null,
                null,
                null,
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
                t.getCreatedAt(),
                t.getUpdatedAt(),
                saldoNegativo
        );
    }
}
