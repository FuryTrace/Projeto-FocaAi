package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;

public class MainActivity extends AppCompatActivity {

    private static final long PRAZO_LOGIN = 30L * 24 * 60 * 60 * 1000;

    private Button buttonLogarInicio;
    private Button buttonCadastrarInicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);

        long idUsuario = preferencias.getLong("id_usuario", -1);
        long dataLogin = preferencias.getLong("data_login", 0);

        boolean loginValido = idUsuario != -1 && dataLogin > 0 && System.currentTimeMillis() - dataLogin < PRAZO_LOGIN;

        if (loginValido) {

            Intent intent = new Intent(MainActivity.this, PrincipalActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
            return;
        }

        // Se o prazo expirou, limpar os dados da sessão.
        preferencias.edit().remove("id_usuario").remove("nome_usuario").remove("email_usuario").remove("data_login").apply();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

                    return insets;
                }
        );

        buttonLogarInicio = findViewById(R.id.buttonLogarInicio);
        buttonCadastrarInicio = findViewById(R.id.buttonCadastrarInicio);

        buttonLogarInicio.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        buttonCadastrarInicio.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }
}