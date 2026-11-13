package com.example.focaai.api.dto;

public class AlterarSenhaRequest {

    private String email;
    private String senhaAtual;
    private String novaSenha;

    public AlterarSenhaRequest(String email, String senhaAtual, String novaSenha) {
        this.email = email;
        this.senhaAtual = senhaAtual;
        this.novaSenha = novaSenha;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaAtual() {
        return senhaAtual;
    }

    public String getNovaSenha() {
        return novaSenha;
    }
}
