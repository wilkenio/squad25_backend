package com.financeiro.api.dto.transactionDTO;

import com.financeiro.api.domain.enums.TransactionState;
import com.financeiro.api.domain.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TransactionAdvancedFilterDTO(
    List<UUID> contaIds,
    List<UUID> categoriaIds,
    TransactionType tipo, 
    TransactionState estado, 
    LocalDateTime dataInicio,
    LocalDateTime dataFim
) {}
