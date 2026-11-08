package com.example.focaai.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SessaoDao {

    @Insert
    long inserir(Sessao sessao);

    @Query("SELECT * FROM sessoes WHERE usuarioId = :usuarioId ORDER BY dataInicio DESC")
    List<Sessao> listarPorUsuario(long usuarioId);

    @Query("SELECT COALESCE(SUM(duracaoMinutos), 0) FROM sessoes WHERE usuarioId = :usuarioId")
    int obterTempoTotal(long usuarioId);

    @Query("DELETE FROM sessoes WHERE usuarioId = :usuarioId")
    void excluirTodasPorUsuario(long usuarioId);

    @Delete
    void excluir(Sessao sessao);
}