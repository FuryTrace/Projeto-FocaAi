package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;
import com.example.focaai.database.AppDatabase;
import com.example.focaai.database.Sessao;
import com.example.focaai.utils.AlertaSonoro;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DescansoActivity extends AppCompatActivity {

    private TextView txtCronometroDescanso;
    private TextView txtMensagemDescanso;
    private LinearLayout linhaCiclosDescanso;
    private Button btnIgnorarDescanso;
    private Button btnEncerrarDescanso;

    private CountDownTimer cronometro;
    private long tempoRestante;
    private long dataInicio;
    private long usuarioId;
    private long tempoFocoAcumulado;

    private int disciplinaId;
    private int blocoAtual;
    private int proximoBloco;
    private int totalBlocos;
    private int duracaoFocoMinutos;
    private String nomeDisciplina;

    private boolean pausaLonga;
    private boolean sessaoSalva;
    private boolean finalizando = false;

    private AppDatabase bancoDados;
    private ExecutorService executor;

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
        btnIgnorarDescanso = findViewById(R.id.btnIgnorarDescanso);
        btnEncerrarDescanso = findViewById(R.id.btnEncerrarDescanso);

        SharedPreferences prefs = getSharedPreferences("FocaAi", MODE_PRIVATE);
        usuarioId = prefs.getLong("id_usuario", -1);
        bancoDados = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        Intent origem = getIntent();
        tempoRestante = origem.getLongExtra("duracao_descanso", 5 * 60 * 1000L);
        disciplinaId = origem.getIntExtra("disciplina_id", -1);
        nomeDisciplina = origem.getStringExtra("disciplina_nome");
        dataInicio = origem.getLongExtra("data_inicio", System.currentTimeMillis());
        blocoAtual = origem.getIntExtra("bloco_atual", 1);
        proximoBloco = origem.getIntExtra("proximo_bloco", blocoAtual + 1);
        totalBlocos = origem.getIntExtra("total_blocos", prefs.getInt("quantidade_blocos", 4));
        duracaoFocoMinutos = origem.getIntExtra("duracao_foco_minutos", 25);
        tempoFocoAcumulado = origem.getLongExtra("tempo_foco_acumulado", 0L);
        pausaLonga = origem.getBooleanExtra("pausa_longa", false);
        sessaoSalva = origem.getBooleanExtra("sessao_salva", false);

        atualizarMensagem();
        atualizarBlocos();
        atualizarCronometro();

        btnIgnorarDescanso.setOnClickListener(v -> finalizarDescanso());
        btnEncerrarDescanso.setOnClickListener(v -> confirmarEncerramento());

        iniciarCronometro();
    }

    private void atualizarMensagem() {
        if (pausaLonga) {
            txtMensagemDescanso.setText(
                    "Parabéns! Você concluiu todos os " + totalBlocos +
                    " blocos de foco de " + nomeDisciplina +
                    ". Aproveite sua pausa longa!"
            );
        } else {
            txtMensagemDescanso.setText(
                    "Muito bem! Bloco " + blocoAtual + " concluído.\n\n" +
                    "Descanse um pouco antes do próximo bloco de " + nomeDisciplina + "."
            );
        }
    }

    private void atualizarBlocos() {
        linhaCiclosDescanso.removeAllViews();
        for (int i = 1; i <= totalBlocos; i++) {
            TextView indicador = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(48), dp(48));
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            indicador.setLayoutParams(params);
            indicador.setBackgroundResource(R.drawable.circulo_ciclo_foca);
            indicador.setGravity(android.view.Gravity.CENTER);
            indicador.setTextSize(18);
            indicador.setTextColor(getColor(R.color.foca_text));
            indicador.setText(i <= blocoAtual ? "✓" : String.valueOf(i));
            linhaCiclosDescanso.addView(indicador);
        }
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void atualizarCronometro() {
        long segundos = tempoRestante / 1000;
        txtCronometroDescanso.setText(String.format("%02d:%02d", segundos / 60, segundos % 60));
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

                if (pausaLonga) {
                    // A pausa longa terminou: a sessão foi concluída.
                    // O aviso será emitido na tela de conclusão.
                } else {
                    // A pausa curta terminou: hora de voltar ao foco.
                    AlertaSonoro.tocarFimDaPausa();
                }

                finalizarDescanso();
            }
        }.start();
    }

    private void finalizarDescanso() {
        if (finalizando) return;
        finalizando = true;
        if (cronometro != null) cronometro.cancel();

        if (pausaLonga) {
            abrirTelaConclusao(false);
        } else {
            Intent intent = new Intent(this, SessaoEmAndamentoActivity.class);
            intent.putExtra("disciplina_id", disciplinaId);
            intent.putExtra("disciplina_nome", nomeDisciplina);
            intent.putExtra("data_inicio", dataInicio);
            intent.putExtra("bloco_atual", proximoBloco);
            intent.putExtra("total_blocos", totalBlocos);
            intent.putExtra("duracao_foco_minutos", duracaoFocoMinutos);
            intent.putExtra("tempo_foco_acumulado", tempoFocoAcumulado);
            startActivity(intent);
            finish();
        }
    }

    private void confirmarEncerramento() {
        new AlertDialog.Builder(this)
                .setTitle("Encerrar sessão?")
                .setMessage("Deseja encerrar a sessão agora? Somente o tempo de foco será registrado no histórico.")
                .setNegativeButton("Continuar descanso", null)
                .setPositiveButton("Encerrar sessão", (dialog, which) -> {
                    if (cronometro != null) cronometro.cancel();
                    if (sessaoSalva) {
                        abrirTelaConclusao(true);
                    } else {
                        salvarSessaoEConcluir();
                    }
                })
                .show();
    }

    private void salvarSessaoEConcluir() {
        if (finalizando) return;
        finalizando = true;

        int minutos = (int) (tempoFocoAcumulado / 60_000L);
        if (minutos <= 0) {
            abrirTelaConclusao(true);
            return;
        }

        Sessao sessao = new Sessao(
                usuarioId, disciplinaId, nomeDisciplina,
                dataInicio, System.currentTimeMillis(), minutos
        );

        executor.execute(() -> {
            bancoDados.sessaoDao().inserir(sessao);
            runOnUiThread(() -> abrirTelaConclusao(true));
        });
    }

    private void abrirTelaConclusao(boolean encerradaManualmente) {
        Intent intent = new Intent(this, DescansoConcluidoActivity.class);
        intent.putExtra("encerrada_manualmente", encerradaManualmente);
        intent.putExtra("sessao_completa", pausaLonga);
        intent.putExtra("tempo_foco_acumulado", tempoFocoAcumulado);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cronometro != null) cronometro.cancel();
        if (executor != null) executor.shutdown();
    }
}
