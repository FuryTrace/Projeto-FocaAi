package com.example.focaai.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Disciplina.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instancia;

    public abstract DisciplinaDao disciplinaDao();

    public static AppDatabase getInstance(Context context) {

        if (instancia == null) {

            synchronized (AppDatabase.class) {

                if (instancia == null) {

                    instancia = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "focaai_database"
                    ).build();
                }
            }
        }

        return instancia;
    }
}