package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;
import com.example.focaai.database.AppDatabase;
import com.example.focaai.database.Disciplina;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdicionarDisciplinaActivity extends AppCompatActivity {

    private EditText edtNomeDisciplina;
    private Button btnSalvarDisciplina;
    private Button btnVoltar;
    private ImageButton btnConfiguracoes;
    private TextView txtData;

    private AppDatabase bancoDados;
    private ExecutorService executor;

    private long usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_adicionar_disciplina);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtNomeDisciplina = findViewById(R.id.edtNomeDisciplina);
        btnSalvarDisciplina = findViewById(R.id.btnSalvarDisciplina);
        btnVoltar = findViewById(R.id.btnVoltarAdicionarDisciplina);
        btnConfiguracoes = findViewById(R.id.btnConfiguracoesAdicionarDisciplina);
        txtData = findViewById(R.id.txtDataAdicionarDisciplina);

        bancoDados = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();

        SharedPreferences preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);

        usuarioId = preferencias.getLong("id_usuario", -1);

        mostrarData();

        btnSalvarDisciplina.setOnClickListener(v -> salvarDisciplina());

        btnVoltar.setOnClickListener(v -> finish());

        btnConfiguracoes.setOnClickListener(v -> {
            Intent intent = new Intent(AdicionarDisciplinaActivity.this, ConfiguracoesActivity.class);
            startActivity(intent);
        });
    }

    private void mostrarData() {

        Locale idioma = new Locale("pt", "BR");

        SimpleDateFormat formato = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", idioma);

        String data = formato.format(new Date());

        data = data.substring(0, 1).toUpperCase(idioma) + data.substring(1);

        txtData.setText(data);
    }

    private void salvarDisciplina() {

        String nome = edtNomeDisciplina.getText().toString().trim();

        if (nome.isEmpty()) {
            edtNomeDisciplina.setError("Informe o nome da disciplina.");
            edtNomeDisciplina.requestFocus();
            return;
        }

        if (usuarioId <= 0) {
            Toast.makeText(this, "Não foi possível identificar o usuário. Faça login novamente.", Toast.LENGTH_LONG).show();
            return;
        }

        btnSalvarDisciplina.setEnabled(false);

        executor.execute(() -> {

            int quantidade = bancoDados.disciplinaDao().contarPorNome(usuarioId, nome);

            if (quantidade > 0) {

                runOnUiThread(() -> {
                    btnSalvarDisciplina.setEnabled(true);

                    edtNomeDisciplina.setError("Essa disciplina já está cadastrada.");
                    edtNomeDisciplina.requestFocus();
                });

                return;
            }

            Disciplina disciplina = new Disciplina(usuarioId, nome);

            bancoDados.disciplinaDao().inserir(disciplina);

            runOnUiThread(() -> {

                btnSalvarDisciplina.setEnabled(true);

                Toast.makeText(AdicionarDisciplinaActivity.this, "Disciplina cadastrada com sucesso!", Toast.LENGTH_SHORT).show();

                edtNomeDisciplina.setText("");
                edtNomeDisciplina.requestFocus();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (executor != null) {
            executor.shutdown();
        }
    }
}