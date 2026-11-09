package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;
import com.example.focaai.database.AppDatabase;
import com.example.focaai.database.Sessao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoricoSessoesActivity extends AppCompatActivity {

    private TextView txtTempoTotal;
    private TextView txtHistoricoVazio;
    private LinearLayout listaHistorico;

    private Button btnVoltarHistorico;
    private Button btnExcluirHistorico;
    private ImageButton btnConfiguracoesHistorico;

    private AppDatabase bancoDados;
    private ExecutorService executor;

    private long usuarioId;

    private final Locale idioma = new Locale("pt", "BR");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_historico_sessoes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtTempoTotal = findViewById(R.id.txtTempoTotal);
        txtHistoricoVazio = findViewById(R.id.txtHistoricoVazio);

        listaHistorico = findViewById(R.id.listaHistorico);

        btnVoltarHistorico = findViewById(R.id.btnVoltarHistorico);
        btnExcluirHistorico = findViewById(R.id.btnExcluirHistorico);
        btnConfiguracoesHistorico = findViewById(R.id.btnConfiguracoesHistorico);

        bancoDados = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        SharedPreferences preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);
        usuarioId = preferencias.getLong("id_usuario", -1);

        btnVoltarHistorico.setOnClickListener(v -> finish());

        btnConfiguracoesHistorico.setOnClickListener(v -> {
            Intent intent = new Intent(HistoricoSessoesActivity.this, ConfiguracoesActivity.class);

            startActivity(intent);
        });

        btnExcluirHistorico.setOnClickListener(v -> confirmarExclusaoTotal());

        carregarHistorico();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (executor != null && !executor.isShutdown()) {
            carregarHistorico();
        }
    }

    private void carregarHistorico() {

        if (usuarioId <= 0) {
            Toast.makeText(this, "Não foi possível identificar o usuário.", Toast.LENGTH_LONG).show();

            finish();
            return;
        }

        executor.execute(() -> {

            List<Sessao> sessoes = bancoDados.sessaoDao().listarPorUsuario(usuarioId);

            int tempoTotal = bancoDados.sessaoDao().obterTempoTotal(usuarioId);

            runOnUiThread(() -> {

                if (isFinishing() || isDestroyed()) {
                    return;
                }

                atualizarTempoTotal(tempoTotal);
                mostrarSessoes(sessoes);

            });
        });
    }

    private void atualizarTempoTotal(int minutos) {

        int horas = minutos / 60;
        int minutosRestantes = minutos % 60;

        if (horas > 0) {

            txtTempoTotal.setText(
                    horas + (horas == 1 ? " hora" : " horas") +
                            " e " +
                            minutosRestantes +
                            (minutosRestantes == 1 ? " minuto" : " minutos")
            );

        } else {

            txtTempoTotal.setText(
                    minutos +
                            (minutos == 1 ? " minuto" : " minutos")
            );
        }
    }

    private void mostrarSessoes(List<Sessao> sessoes) {

        listaHistorico.removeAllViews();

        if (sessoes.isEmpty()) {

            txtHistoricoVazio.setVisibility(View.VISIBLE);
            btnExcluirHistorico.setVisibility(View.GONE);

            return;
        }

        txtHistoricoVazio.setVisibility(View.GONE);
        btnExcluirHistorico.setVisibility(View.VISIBLE);

        for (Sessao sessao : sessoes) {
            criarCardSessao(sessao);
        }
    }

    private void criarCardSessao(Sessao sessao) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 16, 18, 16);

        GradientDrawable fundo = new GradientDrawable();
        fundo.setColor(Color.parseColor("#55FFFFFF"));
        fundo.setCornerRadius(20);
        fundo.setStroke(1, Color.parseColor("#55FFFFFF"));

        card.setBackground(fundo);

        LinearLayout.LayoutParams parametrosCard = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        parametrosCard.setMargins(0, 0, 0, 14);

        card.setLayoutParams(parametrosCard);

        TextView txtDisciplina = new TextView(this);

        txtDisciplina.setText(sessao.getNomeDisciplina());
        txtDisciplina.setTextColor(getColor(R.color.foca_text));
        txtDisciplina.setTextSize(17);
        txtDisciplina.setTypeface(null, Typeface.BOLD);

        card.addView(txtDisciplina);

        SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy", idioma);

        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm", idioma);

        String data = formatoData.format(new Date(sessao.getDataInicio()));

        String horaInicio = formatoHora.format(new Date(sessao.getDataInicio()));

        String horaFim = formatoHora.format(new Date(sessao.getDataFim()));

        TextView txtDataHora = new TextView(this);

        txtDataHora.setText(data + "  •  " + horaInicio + " até " + horaFim);

        txtDataHora.setTextColor(getColor(R.color.foca_text));
        txtDataHora.setTextSize(13);

        LinearLayout.LayoutParams parametrosTexto = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        parametrosTexto.setMargins(0, 8, 0, 0);

        txtDataHora.setLayoutParams(parametrosTexto);

        card.addView(txtDataHora);

        TextView txtDuracao = new TextView(this);

        int duracao = sessao.getDuracaoMinutos();

        txtDuracao.setText(
                "Tempo de foco: " +
                        duracao +
                        (duracao == 1 ? " minuto" : " minutos")
        );

        txtDuracao.setTextColor(getColor(R.color.foca_text));
        txtDuracao.setTextSize(14);
        txtDuracao.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams parametrosDuracao =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametrosDuracao.setMargins(0, 6, 0, 0);

        txtDuracao.setLayoutParams(parametrosDuracao);

        card.addView(txtDuracao);

        Button btnExcluir = new Button(this);

        btnExcluir.setText("Excluir sessão");
        btnExcluir.setAllCaps(false);
        btnExcluir.setTextSize(13);
        btnExcluir.setTextColor(getColor(R.color.foca_text));
        btnExcluir.setBackgroundColor(Color.TRANSPARENT);
        btnExcluir.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        btnExcluir.setMinHeight(48);

        LinearLayout.LayoutParams parametrosBotao = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 48);

        parametrosBotao.setMargins(0, 6, 0, 0);

        btnExcluir.setLayoutParams(parametrosBotao);

        btnExcluir.setOnClickListener(v -> confirmarExclusao(sessao));

        card.addView(btnExcluir);

        listaHistorico.addView(card);
    }

    private void confirmarExclusao(Sessao sessao) {

        new AlertDialog.Builder(this)
                .setTitle("Excluir sessão")
                .setMessage("Deseja realmente excluir esta sessão do histórico?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {

                    executor.execute(() -> {

                        bancoDados.sessaoDao().excluir(sessao);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Sessão excluída.", Toast.LENGTH_SHORT).show();

                            carregarHistorico();
                        });
                    });
                })
                .show();
    }

    private void confirmarExclusaoTotal() {

        new AlertDialog.Builder(this)
                .setTitle("Limpar histórico")
                .setMessage(
                        "Tem certeza de que deseja excluir todas as sessões?\n\n" +
                                "Essa ação não poderá ser desfeita."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir tudo", (dialog, which) -> {

                    executor.execute(() -> {

                        bancoDados.sessaoDao().excluirTodasPorUsuario(usuarioId);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Histórico excluído.", Toast.LENGTH_SHORT).show();

                            carregarHistorico();
                        });
                    });
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (executor != null) {
            executor.shutdown();
        }
    }
}