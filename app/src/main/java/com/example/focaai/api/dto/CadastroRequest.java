package com.example.focaai.api.dto;

public class CadastroRequest {

    private String nome;
    private String email;
    private String senha;

    public CadastroRequest(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }
}