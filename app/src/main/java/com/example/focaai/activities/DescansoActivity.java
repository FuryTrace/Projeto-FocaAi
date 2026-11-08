package com.example.focaai.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;

public class DescansoActivity extends AppCompatActivity {

    private TextView txtCronometroDescanso;
    private TextView txtMensagemDescanso;
    private LinearLayout linhaCiclosDescanso;

    private Button btnVoltarDescanso;

    private CountDownTimer cronometro;

    private long tempoRestante;
    private int cicloAtual;
    private boolean cicloCompleto;
    private String nomeDisciplina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_descanso);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtCronometroDescanso = findViewById(R.id.txtCronometroDescanso);
        txtMensagemDescanso = findViewById(R.id.txtMensagemDescanso);
        linhaCiclosDescanso = findViewById(R.id.linhaCiclosDescanso);

        btnVoltarDescanso = findViewById(R.id.btnVoltarDescanso);

        btnVoltarDescanso.setOnClickListener(v -> {
            if (cronometro != null) {
                cronometro.cancel();
            }

            finish();
        });

        Intent origem = getIntent();

        tempoRestante = origem.getLongExtra("duracao_descanso", 5 * 60 * 1000L);

        cicloAtual = origem.getIntExtra("ciclo_atual", 1);
        cicloCompleto = origem.getBooleanExtra("ciclo_completo", false);
        nomeDisciplina = origem.getStringExtra("disciplina_nome");

        atualizarMensagem();
        atualizarCiclos();
        atualizarCronometro();

        iniciarCronometro();
    }

    private void atualizarMensagem() {

        if (cicloCompleto) {
            txtMensagemDescanso.setText(
                    "Parabéns! Você concluiu as quatro sessões.\n\n" +
                            "Aproveite sua pausa longa para relaxar!"
            );
        } else {
            txtMensagemDescanso.setText(
                    "Muito bem!\n\n" +
                            "Você concluiu uma sessão de " + nomeDisciplina +
                            ". Aproveite sua pausa curta!"
            );
        }
    }

    private void atualizarCiclos() {

        for (int i = 0; i < linhaCiclosDescanso.getChildCount(); i++) {

            TextView ciclo = (TextView) linhaCiclosDescanso.getChildAt(i);

            if (i < cicloAtual) {
                ciclo.setText("✓");
            } else {
                ciclo.setText(String.valueOf(i + 1));
            }
        }
    }

    private void atualizarCronometro() {

        long segundos = tempoRestante / 1000;
        long minutos = segundos / 60;
        long segundosRestantes = segundos % 60;

        txtCronometroDescanso.setText(String.format("%02d:%02d", minutos, segundosRestantes));
    }

    private void iniciarCronometro() {

        cronometro = new CountDownTimer(tempoRestante, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                tempoRestante = millisUntilFinished;
                atualizarCronometro();
            }

            @Override
            public void onFinish() {

                tempoRestante = 0;
                atualizarCronometro();

                abrirDescansoConcluido();
            }
        }.start();
    }

    private void abrirDescansoConcluido() {

        Intent intent = new Intent(DescansoActivity.this, DescansoConcluidoActivity.class);

        intent.putExtra("ciclo_atual", cicloAtual);
        intent.putExtra("ciclo_completo", cicloCompleto);

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cronometro != null) {
            cronometro.cancel();
        }
    }
}