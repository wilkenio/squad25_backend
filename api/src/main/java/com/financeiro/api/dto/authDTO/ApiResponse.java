package com.financeiro.api.dto.authDTO;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)  // Remove campos nulos do JSON
public class ApiResponse<T> {
    private int statusCode;
    private T body;
    private String error;

    public ApiResponse(int statusCode, T body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public ApiResponse(int statusCode, String error) {
        this.statusCode = statusCode;
        this.error = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public T getBody() {
        return body;
    }

    public String getError() {
        return error;
    }
}