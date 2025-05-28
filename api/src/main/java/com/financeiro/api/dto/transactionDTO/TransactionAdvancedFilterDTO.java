package com.financeiro.api.dto.transactionDTO;

import com.financeiro.api.domain.enums.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TransactionAdvancedFilterDTO(
    List<UUID> contaIds,
    List<UUID> categoriaIds,
    CategoryType categoriaTipo,
    TransactionType transacaoTipo, 
    TransactionState estado,
    Frequency frequencia, 
    LocalDateTime dataInicio,
    LocalDateTime dataFim,
    TransactionOrder ordenacao
) {}
