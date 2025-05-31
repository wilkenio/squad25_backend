package com.financeiro.api.infra.exceptions;

public class TransactionNotFoundException extends RuntimeException {
    // Construtor padrão que você pode já ter
    public TransactionNotFoundException() {
        super("Transação não encontrada."); // Mensagem padrão se nenhuma for fornecida
    }

    // NOVO CONSTRUTOR para aceitar uma mensagem personalizada
    public TransactionNotFoundException(String message) {
        super(message); // Passa a mensagem para o construtor da superclasse (RuntimeException)
    }

    // Você também pode adicionar outros construtores se necessário, 
    // por exemplo, um que aceite uma mensagem e uma causa (Throwable)
    public TransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}