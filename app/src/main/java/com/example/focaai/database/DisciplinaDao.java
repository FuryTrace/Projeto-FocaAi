package com.example.focaai.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DisciplinaDao {

    @Insert
    long inserir(Disciplina disciplina);

    @Query("SELECT * FROM disciplinas WHERE usuarioId = :usuarioId ORDER BY nome ASC")
    List<Disciplina> listarPorUsuario(long usuarioId);

    @Query("SELECT * FROM disciplinas WHERE id = :id AND usuarioId = :usuarioId LIMIT 1")
    Disciplina buscarPorId(int id, long usuarioId);

    @Query("SELECT COUNT(*) FROM disciplinas WHERE usuarioId = :usuarioId AND LOWER(nome) = LOWER(:nome)")
    int contarPorNome(long usuarioId, String nome);

    @Delete
    void excluir(Disciplina disciplina);

    @Query("DELETE FROM disciplinas WHERE usuarioId = :usuarioId")
    void excluirTodasPorUsuario(long usuarioId);
}