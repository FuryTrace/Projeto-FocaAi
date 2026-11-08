package com.example.focaai.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;

public class DescansoConcluidoActivity extends AppCompatActivity {

    private TextView txtMensagemDescansoConcluido;
    private Button btnNovaSessao;
    private Button btnVoltarConcluido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_descanso_concluido);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtMensagemDescansoConcluido = findViewById(R.id.txtMensagemDescansoConcluido);
        btnNovaSessao = findViewById(R.id.btnNovaSessao);
        btnVoltarConcluido = findViewById(R.id.btnVoltarConcluido);

        btnVoltarConcluido.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DescansoConcluidoActivity.this,
                    PrincipalActivity.class
            );

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
        });

        boolean cicloCompleto = getIntent().getBooleanExtra(
                "ciclo_completo",
                false
        );

        if (cicloCompleto) {
            txtMensagemDescansoConcluido.setText(
                    "Ciclo Pomodoro concluído! 🎉\n\n" +
                            "Você completou quatro sessões de foco. " +
                            "Parabéns pela dedicação!"
            );
        } else {
            txtMensagemDescansoConcluido.setText(
                    "Pausa concluída! 🌿\n\n" +
                            "Você está pronto para continuar seus estudos?"
            );
        }

        btnNovaSessao.setOnClickListener(v -> abrirNovaSessao());
    }

    private void abrirNovaSessao() {

        Intent intent = new Intent(DescansoConcluidoActivity.this, IniciarSessaoActivity.class);

        startActivity(intent);
        finish();
    }
}