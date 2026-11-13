package com.example.focaai.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.focaai.database.Disciplina;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GerenciarDisciplinasActivity extends AppCompatActivity {

    private EditText edtNomeDisciplina;
    private Button btnAdicionar;
    private LinearLayout listaDisciplinas;
    private TextView txtVazio;
    private AppDatabase bancoDados;
    private ExecutorService executor;
    private long usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_gerenciar_disciplinas);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        edtNomeDisciplina = findViewById(R.id.edtNomeNovaDisciplina);
        btnAdicionar = findViewById(R.id.btnAdicionarDisciplina);
        listaDisciplinas = findViewById(R.id.listaDisciplinasGerenciar);
        txtVazio = findViewById(R.id.txtDisciplinasVazio);
        Button btnVoltar = findViewById(R.id.btnVoltarGerenciarDisciplinas);

        bancoDados = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        SharedPreferences prefs = getSharedPreferences("FocaAi", MODE_PRIVATE);
        usuarioId = prefs.getLong("id_usuario", -1);

        btnVoltar.setOnClickListener(v -> finish());
        btnAdicionar.setOnClickListener(v -> adicionarDisciplina());

        if (usuarioId <= 0) {
            Toast.makeText(this, "Faça login novamente para continuar.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        carregarDisciplinas();
    }

    private void adicionarDisciplina() {
        String nome = edtNomeDisciplina.getText().toString().trim();
        if (nome.isEmpty()) {
            edtNomeDisciplina.setError("Informe o nome da disciplina.");
            edtNomeDisciplina.requestFocus();
            return;
        }

        btnAdicionar.setEnabled(false);
        executor.execute(() -> {
            int quantidade = bancoDados.disciplinaDao().contarPorNome(usuarioId, nome);
            if (quantidade > 0) {
                runOnUiThread(() -> {
                    btnAdicionar.setEnabled(true);
                    edtNomeDisciplina.setError("Essa disciplina já está cadastrada.");
                    edtNomeDisciplina.requestFocus();
                });
                return;
            }

            bancoDados.disciplinaDao().inserir(new Disciplina(usuarioId, nome));
            runOnUiThread(() -> {
                btnAdicionar.setEnabled(true);
                edtNomeDisciplina.setText("");
                Toast.makeText(this, "Disciplina adicionada!", Toast.LENGTH_SHORT).show();
                carregarDisciplinas();
            });
        });
    }

    private void carregarDisciplinas() {
        executor.execute(() -> {
            List<Disciplina> disciplinas = bancoDados.disciplinaDao().listarPorUsuario(usuarioId);
            runOnUiThread(() -> exibirDisciplinas(disciplinas));
        });
    }

    private void exibirDisciplinas(List<Disciplina> disciplinas) {
        listaDisciplinas.removeAllViews();
        boolean vazio = disciplinas == null || disciplinas.isEmpty();
        txtVazio.setVisibility(vazio ? TextView.VISIBLE : TextView.GONE);
        if (vazio) return;

        for (Disciplina disciplina : disciplinas) {
            LinearLayout linha = new LinearLayout(this);
            linha.setOrientation(LinearLayout.HORIZONTAL);
            linha.setGravity(Gravity.CENTER_VERTICAL);
            linha.setPadding(dp(14), dp(8), dp(8), dp(8));
            linha.setBackgroundResource(R.drawable.card_foca);

            LinearLayout.LayoutParams linhaParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(64));
            linhaParams.bottomMargin = dp(10);
            linha.setLayoutParams(linhaParams);

            TextView nome = new TextView(this);
            nome.setText(disciplina.getNome());
            nome.setTextColor(getColor(R.color.foca_text));
            nome.setTextSize(16);
            nome.setGravity(Gravity.CENTER_VERTICAL);
            nome.setSingleLine(true);
            nome.setEllipsize(android.text.TextUtils.TruncateAt.END);
            nome.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));

            Button excluir = new Button(this);
            excluir.setText("Remover");
            excluir.setAllCaps(false);
            excluir.setTextColor(getColor(R.color.foca_text));
            excluir.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getColor(android.R.color.transparent)));
            excluir.setMinWidth(dp(88));
            excluir.setOnClickListener(v -> confirmarExclusao(disciplina));

            linha.addView(nome);
            linha.addView(excluir);
            listaDisciplinas.addView(linha);
        }
    }

    private void confirmarExclusao(Disciplina disciplina) {
        new AlertDialog.Builder(this)
                .setTitle("Remover disciplina")
                .setMessage("Deseja remover \"" + disciplina.getNome() + "\" da sua lista? O histórico de sessões já realizado será mantido.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Remover", (dialog, which) -> excluirDisciplina(disciplina))
                .show();
    }

    private void excluirDisciplina(Disciplina disciplina) {
        executor.execute(() -> {
            bancoDados.disciplinaDao().excluir(disciplina);
            runOnUiThread(() -> {
                Toast.makeText(this, "Disciplina removida.", Toast.LENGTH_SHORT).show();
                carregarDisciplinas();
            });
        });
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (listaDisciplinas != null && executor != null && !executor.isShutdown()) {
            carregarDisciplinas();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) executor.shutdown();
    }
}
