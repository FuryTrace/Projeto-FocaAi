package com.example.focaai.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sessoes")
public class Sessao {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private long usuarioId;
    private int disciplinaId;
    private String nomeDisciplina;
    private long dataInicio;
    private long dataFim;
    private int duracaoMinutos;

    public Sessao(long usuarioId, int disciplinaId, String nomeDisciplina, long dataInicio, long dataFim, int duracaoMinutos) {
        this.usuarioId = usuarioId;
        this.disciplinaId = disciplinaId;
        this.nomeDisciplina = nomeDisciplina;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.duracaoMinutos = duracaoMinutos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public int getDisciplinaId() {
        return disciplinaId;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public long getDataInicio() {
        return dataInicio;
    }

    public long getDataFim() {
        return dataFim;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }
}