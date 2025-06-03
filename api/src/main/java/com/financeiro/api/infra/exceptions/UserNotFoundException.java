package com.financeiro.api.infra.exceptions; // Ou o pacote onde sua exceção está localizada

public class UserNotFoundException extends RuntimeException { // Ou qualquer que seja a superclasse

    // Este é um construtor padrão que você pode já ter
    public UserNotFoundException() {
        super("Usuário não encontrado."); // Mensagem padrão
    }

    // ADICIONE ESTE CONSTRUTOR:
    /**
     * Construtor que aceita uma mensagem personalizada.
     * @param message A mensagem detalhando a exceção.
     */
    public UserNotFoundException(String message) {
        super(message); // Passa a mensagem para o construtor da classe pai (RuntimeException)
    }

    // OPCIONAL: Você também pode querer um construtor que aceite a mensagem e a causa original do erro
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}