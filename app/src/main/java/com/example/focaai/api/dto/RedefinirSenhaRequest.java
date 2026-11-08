package com.example.focaai.api.dto;

public class RedefinirSenhaRequest {

    private String email;
    private String token;
    private String novaSenha;

    public RedefinirSenhaRequest(String email, String token, String novaSenha) {
        this.email = email;
        this.token = token;
        this.novaSenha = novaSenha;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public String getNovaSenha() {
        return novaSenha;
    }
}