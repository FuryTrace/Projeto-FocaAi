package com.example.focaai.api.dto;

public class ExcluirContaRequest {

    private String email;
    private String senha;

    public ExcluirContaRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }
}