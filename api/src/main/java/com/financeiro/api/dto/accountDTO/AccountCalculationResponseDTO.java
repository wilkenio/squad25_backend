package com.financeiro.api.dto.accountDTO;

import com.financeiro.api.domain.Category;

public record AccountCalculationResponseDTO(
        Category category,
        String categoryName,
        String accountDescription,
        String additionalInformation,
        //dados separados do calculo
        Double saldoInicial,
        Double chequeEspecial,
        Double receitas,
        Double despesas,
        Double receitasPrevistas,
        Double despesasPrevistas,
        Double saldo,
        Double saldoPrevisto,
        //calculo total da conta
        Double receitaTotal,
        Double despesaTotal,
        Double saldoTotal
) {
}
