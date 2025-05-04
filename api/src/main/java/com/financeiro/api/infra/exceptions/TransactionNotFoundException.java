package com.financeiro.api.infra.exceptions;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException() {
        super("Transação não encontrada");
    }
}