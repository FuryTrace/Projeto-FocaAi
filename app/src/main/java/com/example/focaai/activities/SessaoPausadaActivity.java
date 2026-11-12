package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SessaoPausadaActivity extends AppCompatActivity {

    private TextView txtCronometroPausado;
    private TextView txtStatusPausada;
    private LinearLayout linhaCiclosPausada;
    private Button btnRetomarSessao;
    private Button btnEncerrarPausada;

    private long tempoRestante;
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
        btnEncerrarPausada = findViewById(R.id.btnEncerrarPausada);

        SharedPreferences prefs = getSharedPreferences("FocaAi", MODE_PRIVATE);
        usuarioId = prefs.getLong("id_usuario", -1);
        bancoDados = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        Intent origem = getIntent();
        tempoRestante = origem.getLongExtra("tempo_restante", 0);
        dataInicio = origem.getLongExtra("data_inicio", System.currentTimeMillis());
        disciplinaId = origem.getIntExtra("disciplina_id", -1);
        nomeDisciplina = origem.getStringExtra("disciplina_nome");
        blocoAtual = origem.getIntExtra("bloco_atual", 1);
        totalBlocos = origem.getIntExtra("total_blocos", prefs.getInt("quantidade_blocos", 4));
        duracaoFocoMinutos = origem.getIntExtra("duracao_foco_minutos", 25);
        tempoFocoAcumulado = origem.getLongExtra("tempo_foco_acumulado", 0L);

        txtCronometroPausado.setText(formatarTempo(tempoRestante));
        txtStatusPausada.setText(nomeDisciplina + "\nBloco " + blocoAtual + " de " + totalBlocos + " • Pausado");
        atualizarBlocos();

        btnRetomarSessao.setOnClickListener(v -> retomarSessao());
        btnEncerrarPausada.setOnClickListener(v -> confirmarEncerramento());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarEncerramento();
            }
        });
    }

    private String formatarTempo(long millis) {
        long segundos = millis / 1000;
        return String.format("%02d:%02d", segundos / 60, segundos % 60);
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void atualizarBlocos() {
        linhaCiclosPausada.removeAllViews();
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
            linhaCiclosPausada.addView(indicador);
        }
    }

    private void retomarSessao() {
        Intent intent = new Intent(this, SessaoEmAndamentoActivity.class);
        intent.putExtra("tempo_restante", tempoRestante);
        intent.putExtra("data_inicio", dataInicio);
        intent.putExtra("disciplina_id", disciplinaId);
        intent.putExtra("disciplina_nome", nomeDisciplina);
        intent.putExtra("bloco_atual", blocoAtual);
        intent.putExtra("total_blocos", totalBlocos);
        intent.putExtra("duracao_foco_minutos", duracaoFocoMinutos);
        intent.putExtra("tempo_foco_acumulado", tempoFocoAcumulado);
        startActivity(intent);
        finish();
    }

    private void confirmarEncerramento() {
        new AlertDialog.Builder(this)
                .setTitle("Encerrar sessão?")
                .setMessage("Deseja encerrar esta sessão pausada? O tempo de foco já realizado será salvo no histórico.")
                .setNegativeButton("Continuar sessão", null)
                .setPositiveButton("Encerrar sessão", (dialog, which) -> salvarSessao())
                .show();
    }

    private void salvarSessao() {
        if (finalizando) return;
        finalizando = true;

        int minutos = (int) (tempoFocoAcumulado / 60_000L);
        if (minutos <= 0) {
            abrirConclusao();
            return;
        }

        Sessao sessao = new Sessao(
                usuarioId, disciplinaId, nomeDisciplina,
                dataInicio, System.currentTimeMillis(), minutos
        );

        executor.execute(() -> {
            bancoDados.sessaoDao().inserir(sessao);
            runOnUiThread(this::abrirConclusao);
        });
    }

    private void abrirConclusao() {
        Intent intent = new Intent(this, DescansoConcluidoActivity.class);
        intent.putExtra("encerrada_manualmente", true);
        intent.putExtra("tempo_foco_acumulado", tempoFocoAcumulado);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}
