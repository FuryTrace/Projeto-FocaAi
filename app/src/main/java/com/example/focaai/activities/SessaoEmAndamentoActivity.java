package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.OnBackPressedCallback;

import com.example.focaai.R;
import com.example.focaai.database.AppDatabase;
import com.example.focaai.database.Sessao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SessaoEmAndamentoActivity extends AppCompatActivity {

    private static final int FOCO_PADRAO = 25;

    private TextView txtCronometro;
    private TextView txtStatusSessao;
    private TextView ciclo1, ciclo2, ciclo3, ciclo4;
    private Button btnPausarSessao;
    private Button btnVoltarSessao;

    private CountDownTimer cronometro;

    private long tempoRestante;
    private long dataInicio;
    private long usuarioId;

    private int disciplinaId;
    private int cicloAtual;
    private int duracaoFocoMinutos;

    private String nomeDisciplina;

    private AppDatabase bancoDados;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_sessao_em_andamento);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtCronometro = findViewById(R.id.txtCronometro);
        txtStatusSessao = findViewById(R.id.txtStatusSessao);

        ciclo1 = findViewById(R.id.ciclo1);
        ciclo2 = findViewById(R.id.ciclo2);
        ciclo3 = findViewById(R.id.ciclo3);
        ciclo4 = findViewById(R.id.ciclo4);

        btnPausarSessao = findViewById(R.id.btnPausarSessao);

        btnVoltarSessao = findViewById(R.id.btnVoltarSessao);

        btnVoltarSessao.setOnClickListener(v -> confirmarSaida());

        bancoDados = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        SharedPreferences preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);

        usuarioId = preferencias.getLong("id_usuario", -1);

        Intent origem = getIntent();

        disciplinaId = origem.getIntExtra("disciplina_id", -1);
        nomeDisciplina = origem.getStringExtra("disciplina_nome");

        cicloAtual = origem.getIntExtra(
                "ciclo_atual",
                preferencias.getInt("ciclo_pomodoro", 0)
        );

        // Se for uma nova sessão, lê a duração salva nas configurações.
        // Se for uma retomada, mantém a duração original da sessão.
        duracaoFocoMinutos = origem.getIntExtra(
                "duracao_foco_minutos",
                preferencias.getInt("tempo_foco_minutos", FOCO_PADRAO)
        );

        tempoRestante = origem.getLongExtra(
                "tempo_restante",
                duracaoFocoMinutos * 60 * 1000L
        );

        dataInicio = origem.getLongExtra(
                "data_inicio",
                System.currentTimeMillis()
        );

        if (usuarioId <= 0 || disciplinaId <= 0 || nomeDisciplina == null) {
            Toast.makeText(this, "Não foi possível iniciar a sessão.", Toast.LENGTH_LONG).show();

            finish();
            return;
        }

        txtStatusSessao.setText(nomeDisciplina + "\nSessão em andamento");

        atualizarCiclos();

        btnPausarSessao.setOnClickListener(v -> pausarSessao());

        iniciarCronometro();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarSaida();
            }
        });
    }

    private void iniciarCronometro() {

        atualizarTextoCronometro();

        cronometro = new CountDownTimer(tempoRestante, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                tempoRestante = millisUntilFinished;
                atualizarTextoCronometro();
            }

            @Override
            public void onFinish() {
                tempoRestante = 0;
                atualizarTextoCronometro();
                concluirSessao();
            }
        }.start();
    }

    private void atualizarTextoCronometro() {

        long segundos = tempoRestante / 1000;
        long minutos = segundos / 60;
        long segundosRestantes = segundos % 60;

        txtCronometro.setText(String.format("%02d:%02d", minutos, segundosRestantes));
    }

    private void atualizarCiclos() {

        TextView[] ciclos = {ciclo1, ciclo2, ciclo3, ciclo4};

        for (int i = 0; i < ciclos.length; i++) {
            if (i < cicloAtual) {
                ciclos[i].setText("✓");
            } else {
                ciclos[i].setText(String.valueOf(i + 1));
            }
        }
    }

    private void pausarSessao() {

        if (cronometro != null) {
            cronometro.cancel();
        }

        Intent intent = new Intent(SessaoEmAndamentoActivity.this, SessaoPausadaActivity.class);

        intent.putExtra("tempo_restante", tempoRestante);
        intent.putExtra("data_inicio", dataInicio);
        intent.putExtra("disciplina_id", disciplinaId);
        intent.putExtra("disciplina_nome", nomeDisciplina);
        intent.putExtra("ciclo_atual", cicloAtual);
        intent.putExtra("duracao_foco_minutos", duracaoFocoMinutos);

        startActivity(intent);
        finish();
    }

    private void concluirSessao() {

        btnPausarSessao.setEnabled(false);

        long dataFim = System.currentTimeMillis();

        Sessao sessao = new Sessao(usuarioId, disciplinaId, nomeDisciplina, dataInicio, dataFim, duracaoFocoMinutos);

        int proximoCiclo = cicloAtual + 1;

        boolean cicloCompleto = proximoCiclo >= 4;

        SharedPreferences preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);

        int minutosDescanso;

        if (cicloCompleto) {
            minutosDescanso = preferencias.getInt("tempo_pausa_longa_minutos", 15);
        } else {
            minutosDescanso = preferencias.getInt("tempo_pausa_curta_minutos", 5);
        }

        long tempoDescanso = minutosDescanso * 60 * 1000L;

        int cicloSalvo = cicloCompleto ? 0 : proximoCiclo;

        getSharedPreferences("FocaAi", MODE_PRIVATE).edit().putInt("ciclo_pomodoro", cicloSalvo).apply();

        executor.execute(() -> {

            bancoDados.sessaoDao().inserir(sessao);

            runOnUiThread(() -> {

                Intent intent = new Intent(SessaoEmAndamentoActivity.this, DescansoActivity.class);

                intent.putExtra("duracao_descanso", tempoDescanso);
                intent.putExtra("ciclo_atual", proximoCiclo);
                intent.putExtra("ciclo_completo", cicloCompleto);
                intent.putExtra("disciplina_nome", nomeDisciplina);

                startActivity(intent);
                finish();
            });
        });
    }

    private void confirmarSaida() {

        new AlertDialog.Builder(this)
                .setTitle("Sair da sessão?")
                .setMessage(
                        "Tem certeza de que deseja sair desta sessão?\n\n" +
                                "O tempo estudado não será registrado no histórico."
                )
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Sim, sair", (dialog, which) -> {
                    if (cronometro != null) {
                        cronometro.cancel();
                    }

                    finish();
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cronometro != null) {
            cronometro.cancel();
        }

        if (executor != null) {
            executor.shutdown();
        }
    }
}