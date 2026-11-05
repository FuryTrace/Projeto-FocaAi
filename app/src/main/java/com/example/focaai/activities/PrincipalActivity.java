package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrincipalActivity extends AppCompatActivity {

    private ImageButton btnConfiguracoes;
    private Button btnIniciarSessao;
    private Button btnHistorico;
    private Button btnAdicionarDisciplina;

    private TextView txtOlaUsuario;
    private TextView txtData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_principal);

        // Ajustar as margens da tela
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Localizar os componentes
        btnConfiguracoes = findViewById(R.id.btnConfiguracoesPrincipal);
        btnIniciarSessao = findViewById(R.id.btnIniciarNovaSessao);
        btnHistorico = findViewById(R.id.btnHistoricoSessoes);
        btnAdicionarDisciplina = findViewById(R.id.btnAdicionarDisciplinaPrincipal);

        txtOlaUsuario = findViewById(R.id.txtOlaUsuario);
        txtData = findViewById(R.id.txtDataPrincipal);

        // Recuperar o nome do usuário
        SharedPreferences preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);

        String nomeUsuario = preferencias.getString("nome_usuario", "Usuário");

        txtOlaUsuario.setText("Olá, " + nomeUsuario + "!");

        // Mostrar a data atual
        Locale idioma = new Locale("pt", "BR");

        SimpleDateFormat formatoData = new SimpleDateFormat("EEEE\ndd 'de' MMMM 'de' yyyy", idioma);

        String dataAtual = formatoData.format(new Date());

        // Deixar a primeira letra do dia em maiúsculo
        dataAtual = dataAtual.substring(0, 1).toUpperCase(idioma) + dataAtual.substring(1);

        txtData.setText(dataAtual);

        // Abrir Configurações
        btnConfiguracoes.setOnClickListener(v -> {
            Intent intent = new Intent(PrincipalActivity.this, ConfiguracoesActivity.class);
            startActivity(intent);
        });

        // Abrir Iniciar Sessão
        btnIniciarSessao.setOnClickListener(v -> {
            Intent intent = new Intent(PrincipalActivity.this, IniciarSessaoActivity.class);
            startActivity(intent);
        });

        // Abrir Histórico
        btnHistorico.setOnClickListener(v -> {
            Intent intent = new Intent(PrincipalActivity.this, HistoricoSessoesActivity.class);
            startActivity(intent);
        });

        // Abrir Adicionar Disciplina
        btnAdicionarDisciplina.setOnClickListener(v -> {
            Intent intent = new Intent(PrincipalActivity.this, AdicionarDisciplinaActivity.class);
            startActivity(intent);
        });
    }
}