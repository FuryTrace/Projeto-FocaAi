package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.focaai.R;

public class ConfiguracoesActivity extends AppCompatActivity {

    private ImageView imgFotoPerfil;
    private TextView txtUsuario;
    private Button btnVoltar;
    private Button btnSairConta;

    private ActivityResultLauncher<String[]> selecionarFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        // Localizar componentes
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        txtUsuario = findViewById(R.id.txtUsuarioConfiguracoes);
        btnVoltar = findViewById(R.id.btnVoltarConfiguracoes);
        btnSairConta = findViewById(R.id.btnSairConta);

        // Recuperar nome e foto salvos
        SharedPreferences preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);

        String nomeUsuario = preferencias.getString("nome_usuario", "Seu nome");

        String fotoSalva = preferencias.getString("foto_perfil", "");

        txtUsuario.setText(nomeUsuario);

        if (!fotoSalva.isEmpty()) {
            imgFotoPerfil.setImageURI(Uri.parse(fotoSalva));
        }

        // Abrir seletor de imagens
        selecionarFoto = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            imgFotoPerfil.setImageURI(uri);

                            preferencias.edit().putString("foto_perfil", uri.toString()).apply();

                            Toast.makeText(this, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            Toast.makeText(this, "Não foi possível carregar a imagem.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Clicar na foto para escolher outra
        imgFotoPerfil.setOnClickListener(v -> {
            selecionarFoto.launch(new String[]{"image/*"});
        });

        // Voltar para a tela anterior
        btnVoltar.setOnClickListener(v -> finish());

        // Sair da conta
        btnSairConta.setOnClickListener(v -> {
            Intent intent = new Intent(ConfiguracoesActivity.this, LoginActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });
    }
}