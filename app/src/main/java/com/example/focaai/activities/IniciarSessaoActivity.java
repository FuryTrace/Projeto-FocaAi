
package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;
import com.example.focaai.database.AppDatabase;
import com.example.focaai.database.Disciplina;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IniciarSessaoActivity extends AppCompatActivity {

    private RadioGroup grupoDisciplinas;
    private Button btnIniciarSessao;
    private Button btnVoltar;
    private ImageButton btnConfiguracoes;

    private AppDatabase bancoDados;
    private ExecutorService executor;

    private long usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_iniciar_sessao);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        grupoDisciplinas = findViewById(R.id.grupoDisciplinas);
        btnIniciarSessao = findViewById(R.id.btnIniciarSessao);
        btnVoltar = findViewById(R.id.btnVoltarIniciarSessao);
        btnConfiguracoes = findViewById(R.id.btnConfiguracoesIniciarSessao);

        bancoDados = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        SharedPreferences preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);

        usuarioId = preferencias.getLong("id_usuario", -1);

        btnVoltar.setOnClickListener(v -> finish());

        btnConfiguracoes.setOnClickListener(v -> {
            Intent intent = new Intent(IniciarSessaoActivity.this, ConfiguracoesActivity.class);
            startActivity(intent);
        });

        btnIniciarSessao.setOnClickListener(v -> iniciarSessao());

        carregarDisciplinas();
    }

    private void carregarDisciplinas() {

        if (usuarioId <= 0) {
            Toast.makeText(this, "Faça login novamente para continuar.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        executor.execute(() -> {

            List<Disciplina> disciplinas = bancoDados
                    .disciplinaDao()
                    .listarPorUsuario(usuarioId);

            runOnUiThread(() -> {

                grupoDisciplinas.removeAllViews();

                if (disciplinas.isEmpty()) {

                    Toast.makeText(this, "Você ainda não cadastrou disciplinas.", Toast.LENGTH_LONG).show();

                    btnIniciarSessao.setEnabled(false);
                    return;
                }

                btnIniciarSessao.setEnabled(true);

                for (Disciplina disciplina : disciplinas) {

                    RadioButton opcao = new RadioButton(this);

                    opcao.setText(disciplina.getNome());
                    opcao.setTextSize(17);
                    opcao.setTextColor(getResources().getColor(R.color.foca_text));

                    opcao.setPadding(12, 12, 12, 12);

                    // Guardar o ID da disciplina selecionada
                    opcao.setTag(disciplina.getId());

                    grupoDisciplinas.addView(opcao);
                }
            });
        });
    }

    private void iniciarSessao() {

        int idSelecionado = grupoDisciplinas.getCheckedRadioButtonId();

        if (idSelecionado == -1) {
            Toast.makeText(this, "Selecione uma disciplina para continuar.", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton opcaoSelecionada = findViewById(idSelecionado);

        if (opcaoSelecionada == null) {
            return;
        }

        int disciplinaId = (int) opcaoSelecionada.getTag();
        String nomeDisciplina = opcaoSelecionada.getText().toString();

        Intent intent = new Intent(IniciarSessaoActivity.this, SessaoEmAndamentoActivity.class);

        intent.putExtra("disciplina_id", disciplinaId);
        intent.putExtra("disciplina_nome", nomeDisciplina);

        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (grupoDisciplinas != null && executor != null) {
            carregarDisciplinas();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (executor != null) {
            executor.shutdown();
        }
    }
}
