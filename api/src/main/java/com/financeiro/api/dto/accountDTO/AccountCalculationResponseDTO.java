package com.financeiro.api.dto.accountDTO;

import java.util.UUID;

public record AccountCalculationResponseDTO(
        UUID id,
        UUID categoryId,
        String categoryName,
        String iconClass,
        String color,
        String accountName,
        String accountDescription,
        //dados separados do calculo
        Double saldoInicial,
        Double chequeEspecial,
        Double receitas,
        Double despesas,
        Double receitasPrevistas,
        Double despesasPrevistas,
        Double saldo,
        Double saldoPrevisto
) {
}
