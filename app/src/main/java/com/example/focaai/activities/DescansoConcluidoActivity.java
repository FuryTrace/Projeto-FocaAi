package com.example.focaai.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;
import com.example.focaai.utils.AlertaSonoro;

public class DescansoConcluidoActivity extends AppCompatActivity {

    private TextView txtMensagemDescansoConcluido;
    private Button btnNovaSessao;
    private Button btnVoltarConcluido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_descanso_concluido);

        // Avisar que a sessão foi encerrada ou concluída.
        AlertaSonoro.tocarSessaoConcluida();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtMensagemDescansoConcluido = findViewById(R.id.txtMensagemDescansoConcluido);
        btnNovaSessao = findViewById(R.id.btnNovaSessao);
        btnVoltarConcluido = findViewById(R.id.btnVoltarConcluido);

        boolean encerradaManualmente = getIntent().getBooleanExtra("encerrada_manualmente", false);

        if (encerradaManualmente) {
            txtMensagemDescansoConcluido.setText(
                    "Sessão encerrada! 🌿\n\n" +
                    "Seu tempo de foco foi registrado no histórico. " +
                    "Você pode começar uma nova sessão quando quiser."
            );
        } else {
            txtMensagemDescansoConcluido.setText(
                    "Sessão Pomodoro concluída! 🎉\n\n" +
                    "Você completou todos os blocos de foco. " +
                    "Parabéns pela dedicação!"
            );
        }

        btnVoltarConcluido.setOnClickListener(v -> voltarAoInicio());
        btnNovaSessao.setOnClickListener(v -> abrirNovaSessao());
    }

    private void voltarAoInicio() {
        Intent intent = new Intent(this, PrincipalActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void abrirNovaSessao() {
        Intent intent = new Intent(this, IniciarSessaoActivity.class);
        startActivity(intent);
        finish();
    }
}
