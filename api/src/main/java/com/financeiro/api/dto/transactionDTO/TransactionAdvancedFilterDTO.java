package com.financeiro.api.dto.transactionDTO;

import com.financeiro.api.domain.enums.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TransactionAdvancedFilterDTO(
    // Filtro Geral
    LocalDateTime dataInicio,
    LocalDateTime dataFim,
    DataReferencia dataReferencia,
    List<UUID> contaIds,
    Boolean mostrarApenasSaldo,
    Boolean incluirSaldoPrevisto,
    Boolean incluirReceitas,
    Boolean incluirReceitasEfetivadas,
    Boolean incluirReceitasPrevistas,
    Boolean incluirDespesas,
    Boolean incluirDespesasEfetivadas,
    Boolean incluirDespesasPrevistas,
    Boolean incluirTransferencias,
    Boolean incluirTransferenciasEfetivadas,
    Boolean incluirTransferenciasPrevistas,

    // Filtros adicionais
    Boolean incluirTodasCategoriasReceita,
    List<UUID> idsCategoriasReceita,
    Boolean incluirTodasCategoriasDespesa,
    List<UUID> idsCategoriasDespesa,
    Boolean incluirFreqNaoRecorrente,      
    Boolean incluirFreqFixaMensal,
    Boolean incluirFreqRepetida,
    
    // Apresentação de dados
    TransactionOrder ordenacao,
    TipoDado tipoDado,
    TipoApresentacaoDados apresentacao, 
    Integer limite,

    // Campos para Paginação
    Integer pageNumber, // Número da página 
    Integer pageSize    // Tamanho da página
    
    
    

    
    
    
    
    
    
    


) {}
