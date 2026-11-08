package com.example.focaai.api.dto;

public class ValidarCodigoRequest {

    private String email;
    private String codigo;

    public ValidarCodigoRequest(String email, String codigo) {
        this.email = email;
        this.codigo = codigo;
    }

    public String getEmail() {
        return email;
    }

    public String getCodigo() {
        return codigo;
    }
}