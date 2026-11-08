package com.example.focaai.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "disciplinas")
public class Disciplina {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private long usuarioId;
    private String nome;

    public Disciplina(long usuarioId, String nome) {
        this.usuarioId = usuarioId;
        this.nome = nome;
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

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}