package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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

public class SessaoEmAndamentoActivity extends AppCompatActivity {

    private TextView txtCronometro;
    private TextView txtStatusSessao;
    private LinearLayout linhaCiclos;
    private Button btnPausarSessao;
    private Button btnEncerrarSessao;

    private CountDownTimer cronometro;
    private long tempoRestante;
    private long tempoTotalBloco;
    private long dataInicio;
    private long usuarioId;
    private long tempoFocoAcumulado;

    private int disciplinaId;
    private int blocoAtual;
    private int totalBlocos;
    private int duracaoFocoMinutos;
    private String nomeDisciplina;

    private AppDatabase bancoDados;
    private ExecutorService executor;
    private boolean finalizando = false;

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
        linhaCiclos = findViewById(R.id.linhaCiclos);
        btnPausarSessao = findViewById(R.id.btnPausarSessao);
        btnEncerrarSessao = findViewById(R.id.btnEncerrarSessao);

        bancoDados = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        SharedPreferences preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);
        usuarioId = preferencias.getLong("id_usuario", -1);

        Intent origem = getIntent();
        disciplinaId = origem.getIntExtra("disciplina_id", -1);
        nomeDisciplina = origem.getStringExtra("disciplina_nome");

        blocoAtual = origem.getIntExtra("bloco_atual", 1);
        totalBlocos = origem.getIntExtra(
                "total_blocos",
                preferencias.getInt("quantidade_blocos", 4)
        );
        duracaoFocoMinutos = origem.getIntExtra(
                "duracao_foco_minutos",
                preferencias.getInt("tempo_foco_minutos", 25)
        );

        tempoFocoAcumulado = origem.getLongExtra("tempo_foco_acumulado", 0L);
        dataInicio = origem.getLongExtra("data_inicio", System.currentTimeMillis());

        tempoTotalBloco = duracaoFocoMinutos * 60_000L;
        tempoRestante = origem.getLongExtra("tempo_restante", tempoTotalBloco);

        if (usuarioId <= 0 || disciplinaId <= 0 || nomeDisciplina == null
                || totalBlocos < 3 || totalBlocos > 5
                || blocoAtual < 1 || blocoAtual > totalBlocos) {
            Toast.makeText(this, "Não foi possível iniciar a sessão.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        txtStatusSessao.setText(nomeDisciplina + "\nBloco " + blocoAtual + " de " + totalBlocos);
        atualizarBlocos();

        btnPausarSessao.setOnClickListener(v -> pausarSessao());
        btnEncerrarSessao.setOnClickListener(v -> confirmarEncerramento());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarEncerramento();
            }
        });

        iniciarCronometro();
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

                // Avisar que o bloco de foco terminou.
                AlertaSonoro.tocarFimDoFoco();

                concluirBlocoDeFoco();
            }
        }.start();
    }

    private void atualizarTextoCronometro() {
        long segundos = tempoRestante / 1000;
        txtCronometro.setText(String.format("%02d:%02d", segundos / 60, segundos % 60));
    }

    private void atualizarBlocos() {
        linhaCiclos.removeAllViews();

        for (int i = 1; i <= totalBlocos; i++) {
            TextView indicador = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(48), dp(48));
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            indicador.setLayoutParams(params);
            indicador.setBackgroundResource(R.drawable.circulo_ciclo_foca);
            indicador.setGravity(android.view.Gravity.CENTER);
            indicador.setTextSize(18);
            indicador.setTextColor(getColor(R.color.foca_text));
            indicador.setText(i < blocoAtual ? "✓" : String.valueOf(i));
            linhaCiclos.addView(indicador);
        }
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }

    private long obterTempoDoBlocoAtual() {
        return Math.max(0L, tempoTotalBloco - tempoRestante);
    }

    private void pausarSessao() {
        if (cronometro != null) cronometro.cancel();

        long acumuladoAtual = tempoFocoAcumulado + obterTempoDoBlocoAtual();

        Intent intent = new Intent(this, SessaoPausadaActivity.class);
        preencherDadosSessao(intent);
        intent.putExtra("tempo_restante", tempoRestante);
        intent.putExtra("tempo_foco_acumulado", acumuladoAtual);
        startActivity(intent);
        finish();
    }

    private void concluirBlocoDeFoco() {
        if (finalizando) return;

        tempoFocoAcumulado += tempoTotalBloco;

        if (blocoAtual >= totalBlocos) {
            salvarSessaoEContinuarParaPausaLonga();
        } else {
            abrirPausaCurta();
        }
    }

    private void preencherDadosSessao(Intent intent) {
        intent.putExtra("disciplina_id", disciplinaId);
        intent.putExtra("disciplina_nome", nomeDisciplina);
        intent.putExtra("data_inicio", dataInicio);
        intent.putExtra("bloco_atual", blocoAtual);
        intent.putExtra("total_blocos", totalBlocos);
        intent.putExtra("duracao_foco_minutos", duracaoFocoMinutos);
        intent.putExtra("tempo_foco_acumulado", tempoFocoAcumulado);
    }

    private void abrirPausaCurta() {
        SharedPreferences prefs = getSharedPreferences("FocaAi", MODE_PRIVATE);
        long duracao = prefs.getInt("tempo_pausa_curta_minutos", 5) * 60_000L;

        Intent intent = new Intent(this, DescansoActivity.class);
        preencherDadosSessao(intent);
        intent.putExtra("duracao_descanso", duracao);
        intent.putExtra("pausa_longa", false);
        intent.putExtra("sessao_salva", false);
        intent.putExtra("proximo_bloco", blocoAtual + 1);
        startActivity(intent);
        finish();
    }

    private void salvarSessaoEContinuarParaPausaLonga() {
        if (finalizando) return;
        finalizando = true;

        salvarRegistroNoHistorico(() -> {
            SharedPreferences prefs = getSharedPreferences("FocaAi", MODE_PRIVATE);
            long duracao = prefs.getInt("tempo_pausa_longa_minutos", 15) * 60_000L;

            Intent intent = new Intent(this, DescansoActivity.class);
            preencherDadosSessao(intent);
            intent.putExtra("duracao_descanso", duracao);
            intent.putExtra("pausa_longa", true);
            intent.putExtra("sessao_salva", true);
            startActivity(intent);
            finish();
        });
    }

    private void confirmarEncerramento() {
        new AlertDialog.Builder(this)
                .setTitle("Encerrar sessão?")
                .setMessage("Deseja encerrar esta sessão de estudos agora? O tempo de foco já realizado será salvo no histórico. O tempo de descanso não será contado.")
                .setNegativeButton("Continuar estudando", null)
                .setPositiveButton("Encerrar sessão", (dialog, which) -> {
                    if (cronometro != null) cronometro.cancel();
                    tempoFocoAcumulado += obterTempoDoBlocoAtual();
                    if (finalizando) return;
                    finalizando = true;
                    salvarRegistroNoHistorico(() -> abrirTelaConclusao(true));
                })
                .show();
    }

    private void salvarRegistroNoHistorico(Runnable aoConcluir) {
        long dataFim = System.currentTimeMillis();
        int minutos = (int) (tempoFocoAcumulado / 60_000L);

        if (minutos <= 0) {
            aoConcluir.run();
            return;
        }

        Sessao sessao = new Sessao(
                usuarioId, disciplinaId, nomeDisciplina,
                dataInicio, dataFim, minutos
        );

        executor.execute(() -> {
            bancoDados.sessaoDao().inserir(sessao);
            runOnUiThread(aoConcluir);
        });
    }

    private void abrirTelaConclusao(boolean encerradaManualmente) {
        Intent intent = new Intent(this, DescansoConcluidoActivity.class);
        intent.putExtra("encerrada_manualmente", encerradaManualmente);
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
