package com.example.focaai.api.dto;

public class SolicitarRecuperacaoRequest {

    private String email;

    public SolicitarRecuperacaoRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}