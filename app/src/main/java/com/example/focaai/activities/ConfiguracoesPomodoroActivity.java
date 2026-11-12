package com.example.focaai.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;

public class ConfiguracoesPomodoroActivity extends AppCompatActivity {

    private static final int FOCO_PADRAO = 25;
    private static final int PAUSA_CURTA_PADRAO = 5;
    private static final int PAUSA_LONGA_PADRAO = 15;
    private static final int BLOCOS_PADRAO = 4;

    private EditText edtTempoFoco;
    private EditText edtPausaCurta;
    private EditText edtPausaLonga;
    private EditText edtQuantidadeBlocos;

    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_configuracoes_pomodoro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtTempoFoco = findViewById(R.id.edtTempoFoco);
        edtPausaCurta = findViewById(R.id.edtPausaCurta);
        edtPausaLonga = findViewById(R.id.edtPausaLonga);
        edtQuantidadeBlocos = findViewById(R.id.edtQuantidadeBlocos);

        Button btnVoltar = findViewById(R.id.btnVoltarPomodoro);
        Button btnSalvar = findViewById(R.id.btnSalvarPomodoro);
        Button btnRestaurar = findViewById(R.id.btnRestaurarPomodoro);

        preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);

        carregarConfiguracoes();

        btnVoltar.setOnClickListener(v -> finish());

        btnSalvar.setOnClickListener(v -> salvarConfiguracoes());

        btnRestaurar.setOnClickListener(v -> confirmarRestauracao());
    }

    private void carregarConfiguracoes() {

        int foco = preferencias.getInt("tempo_foco_minutos", FOCO_PADRAO);
        int pausaCurta = preferencias.getInt("tempo_pausa_curta_minutos", PAUSA_CURTA_PADRAO);
        int pausaLonga = preferencias.getInt("tempo_pausa_longa_minutos", PAUSA_LONGA_PADRAO);
        int blocos = preferencias.getInt("quantidade_blocos", BLOCOS_PADRAO);

        edtTempoFoco.setText(String.valueOf(foco));
        edtPausaCurta.setText(String.valueOf(pausaCurta));
        edtPausaLonga.setText(String.valueOf(pausaLonga));
        edtQuantidadeBlocos.setText(String.valueOf(blocos));
    }

    private void salvarConfiguracoes() {

        String textoFoco = edtTempoFoco.getText().toString().trim();
        String textoPausaCurta = edtPausaCurta.getText().toString().trim();
        String textoPausaLonga = edtPausaLonga.getText().toString().trim();
        String textoBlocos = edtQuantidadeBlocos.getText().toString().trim();

        if (textoFoco.isEmpty() || textoPausaCurta.isEmpty() || textoPausaLonga.isEmpty() || textoBlocos.isEmpty()) {

            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        int foco;
        int pausaCurta;
        int pausaLonga;
        int blocos;

        try {
            foco = Integer.parseInt(textoFoco);
            pausaCurta = Integer.parseInt(textoPausaCurta);
            pausaLonga = Integer.parseInt(textoPausaLonga);
            blocos = Integer.parseInt(textoBlocos);

        } catch (NumberFormatException e) {

            Toast.makeText(this, "Informe valores numéricos válidos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (foco < 1 || foco > 180) {
            edtTempoFoco.setError("Use um valor entre 1 e 180 minutos.");
            edtTempoFoco.requestFocus();
            return;
        }

        if (pausaCurta < 1 || pausaCurta > 60) {
            edtPausaCurta.setError("Use um valor entre 1 e 60 minutos.");
            edtPausaCurta.requestFocus();
            return;
        }

        if (pausaLonga < 1 || pausaLonga > 120) {
            edtPausaLonga.setError("Use um valor entre 1 e 120 minutos.");
            edtPausaLonga.requestFocus();
            return;
        }

        if (blocos < 3 || blocos > 5) {
            edtQuantidadeBlocos.setError("Escolha entre 3 e 5 blocos.");
            edtQuantidadeBlocos.requestFocus();
            return;
        }

        preferencias.edit()
                .putInt("tempo_foco_minutos", foco)
                .putInt("tempo_pausa_curta_minutos", pausaCurta)
                .putInt("tempo_pausa_longa_minutos", pausaLonga)
                .putInt("quantidade_blocos", blocos)
                .apply();

        Toast.makeText(this, "Configurações salvas com sucesso!", Toast.LENGTH_SHORT).show();

        finish();
    }

    private void confirmarRestauracao() {

        new AlertDialog.Builder(this)
                .setTitle("Restaurar configurações padrão")
                .setMessage(
                        "Deseja restaurar as configurações originais?\n\n" +
                                "Foco: 25 minutos\n" +
                                "Pausa curta: 5 minutos\n" +
                                "Pausa longa: 15 minutos\n" +
                                "Quantidade de blocos: 4"
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Restaurar", (dialog, which) -> restaurarConfiguracoes())
                .show();
    }

    private void restaurarConfiguracoes() {

        preferencias.edit()
                .putInt("tempo_foco_minutos", FOCO_PADRAO)
                .putInt("tempo_pausa_curta_minutos", PAUSA_CURTA_PADRAO)
                .putInt("tempo_pausa_longa_minutos", PAUSA_LONGA_PADRAO)
                .putInt("quantidade_blocos", BLOCOS_PADRAO)
                .apply();

        carregarConfiguracoes();

        Toast.makeText(this, "Configurações padrão restauradas!", Toast.LENGTH_SHORT).show();
    }
}