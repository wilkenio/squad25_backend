package com.financeiro.api.dto.accountDTO;

public record AccountCalculationResponseDTO(
        String iconClass,
        String color,
        String accountName,
        String accountDescription,
        String additionalInformation,
        //dados separados do calculo
        Double saldoInicial,
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
