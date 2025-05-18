package com.financeiro.api.dto.transactionDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.financeiro.api.domain.enums.TransactionOrder;

public record TransactionFilterDTO(
    LocalDateTime dataInicio,
    LocalDateTime dataFim,
    List<UUID> contaIds, // Aceita lista ou único UUID
    boolean mostrarReceitas,
    boolean receitasEfetivadas,
    boolean receitasPrevistas,
    boolean mostrarDespesas,
    boolean despesasEfetivadas,
    boolean despesasPrevistas,
    boolean mostrarTransferencias,
    boolean transferenciasEfetivadas,
    boolean transferenciasPrevistas,
    List<UUID> categoriaIds, // Unificado e aceita lista
    TransactionOrder ordenacao
) {}
