package com.example.focaai.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.OnBackPressedCallback;

import com.example.focaai.R;

public class SessaoPausadaActivity extends AppCompatActivity {

    private TextView txtCronometroPausado;
    private TextView txtStatusPausada;
    private LinearLayout linhaCiclosPausada;
    private Button btnRetomarSessao;
    private Button btnVoltarPausada;

    private long tempoRestante;
    private long dataInicio;

    private int disciplinaId;
    private int cicloAtual;
    private int duracaoFocoMinutos;

    private String nomeDisciplina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_sessao_pausada);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtCronometroPausado = findViewById(R.id.txtCronometroPausado);
        txtStatusPausada = findViewById(R.id.txtStatusPausada);
        linhaCiclosPausada = findViewById(R.id.linhaCiclosPausada);
        btnRetomarSessao = findViewById(R.id.btnRetomarSessao);

        btnVoltarPausada = findViewById(R.id.btnVoltarPausada);

        btnVoltarPausada.setOnClickListener(v -> confirmarSaida());

        Intent origem = getIntent();

        tempoRestante = origem.getLongExtra("tempo_restante", 0);
        dataInicio = origem.getLongExtra("data_inicio", System.currentTimeMillis());

        disciplinaId = origem.getIntExtra("disciplina_id", -1);
        nomeDisciplina = origem.getStringExtra("disciplina_nome");

        cicloAtual = origem.getIntExtra("ciclo_atual", 0);

        duracaoFocoMinutos = origem.getIntExtra("duracao_foco_minutos", 25);

        atualizarCronometro();
        atualizarCiclos();

        txtStatusPausada.setText(nomeDisciplina + "\nSessão pausada");

        btnRetomarSessao.setOnClickListener(v -> retomarSessao());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarSaida();
            }
        });
    }

    private void atualizarCronometro() {

        long segundos = tempoRestante / 1000;

        long minutos = segundos / 60;
        long segundosRestantes = segundos % 60;

        txtCronometroPausado.setText(String.format("%02d:%02d", minutos, segundosRestantes));
    }

    private void atualizarCiclos() {

        for (int i = 0; i < linhaCiclosPausada.getChildCount(); i++) {

            TextView ciclo = (TextView) linhaCiclosPausada.getChildAt(i);

            if (i < cicloAtual) {
                ciclo.setText("✓");
            } else {
                ciclo.setText(String.valueOf(i + 1));
            }
        }
    }

    private void retomarSessao() {

        Intent intent = new Intent(SessaoPausadaActivity.this, SessaoEmAndamentoActivity.class);

        intent.putExtra("tempo_restante", tempoRestante);
        intent.putExtra("data_inicio", dataInicio);
        intent.putExtra("disciplina_id", disciplinaId);
        intent.putExtra("disciplina_nome", nomeDisciplina);
        intent.putExtra("ciclo_atual", cicloAtual);
        intent.putExtra("duracao_foco_minutos", duracaoFocoMinutos);

        startActivity(intent);
        finish();
    }

    private void confirmarSaida() {

        new AlertDialog.Builder(this)
                .setTitle("Sair da sessão?")
                .setMessage(
                        "Deseja realmente abandonar esta sessão pausada?\n\n" +
                                "O tempo estudado não será registrado no histórico."
                )
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Sim, sair", (dialog, which) -> finish())
                .show();
    }
}