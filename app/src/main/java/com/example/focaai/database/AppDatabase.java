package com.example.focaai.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Disciplina.class, Sessao.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instancia;

    public abstract DisciplinaDao disciplinaDao();

    public abstract SessaoDao sessaoDao();

    private static final Migration MIGRATION_1_2 =
            new Migration(1, 2) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    database.execSQL("CREATE TABLE IF NOT EXISTS sessoes (" + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + "usuarioId INTEGER NOT NULL, " + "disciplinaId INTEGER NOT NULL, " + "nomeDisciplina TEXT, " + "dataInicio INTEGER NOT NULL, " + "dataFim INTEGER NOT NULL, " + "duracaoMinutos INTEGER NOT NULL)");
                }
            };

    public static AppDatabase getInstance(Context context) {

        if (instancia == null) {

            synchronized (AppDatabase.class) {

                if (instancia == null) {

                    instancia = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "focaai_database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }

        return instancia;
    }
}