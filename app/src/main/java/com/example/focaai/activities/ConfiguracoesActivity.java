package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;
import com.example.focaai.api.RetrofitClient;
import com.example.focaai.api.dto.ExcluirContaRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfiguracoesActivity extends AppCompatActivity {

    private ImageView imgFotoPerfil;
    private TextView txtUsuario;
    private Button btnVoltar;
    private Button btnSairConta;
    private Button btnExcluirConta;

    private SharedPreferences preferencias;

    private ActivityResultLauncher<String[]> selecionarFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_configuracoes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        txtUsuario = findViewById(R.id.txtUsuarioConfiguracoes);
        btnVoltar = findViewById(R.id.btnVoltarConfiguracoes);
        btnSairConta = findViewById(R.id.btnSairConta);
        btnExcluirConta = findViewById(R.id.btnExcluirConta);

        preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);

        carregarDadosUsuario();

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

        imgFotoPerfil.setOnClickListener(v -> selecionarFoto.launch(new String[]{"image/*"}));

        btnVoltar.setOnClickListener(v -> finish());

        btnSairConta.setOnClickListener(v -> confirmarSaida());

        btnExcluirConta.setOnClickListener(v -> confirmarExclusao());
    }

    private void carregarDadosUsuario() {

        String nomeUsuario = preferencias.getString("nome_usuario", "Seu nome");

        String fotoSalva = preferencias.getString("foto_perfil", "");

        txtUsuario.setText(nomeUsuario);

        if (!fotoSalva.isEmpty()) {
            imgFotoPerfil.setImageURI(Uri.parse(fotoSalva));
        }
    }

    private void confirmarSaida() {

        new AlertDialog.Builder(this)
                .setTitle("Sair da conta")
                .setMessage("Deseja realmente sair da sua conta?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sair", (dialog, which) -> {

                    preferencias.edit()
                            .remove("id_usuario")
                            .remove("nome_usuario")
                            .remove("email_usuario")
                            .apply();

                    abrirLogin();
                })
                .show();
    }

    private void confirmarExclusao() {

        EditText campoSenha = new EditText(this);
        campoSenha.setHint("Digite sua senha");
        campoSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        campoSenha.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Excluir conta")
                .setMessage(
                        "Essa ação é permanente. Sua conta será excluída. " +
                                "Para confirmar, informe sua senha."
                )
                .setView(campoSenha)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {

                    String senha = campoSenha.getText().toString();

                    if (senha.isEmpty()) {
                        Toast.makeText(this, "Informe sua senha para continuar.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    excluirConta(senha);
                })
                .show();
    }

    private void excluirConta(String senha) {

        String email = preferencias.getString("email_usuario", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "Não foi possível identificar sua conta. Faça login novamente.", Toast.LENGTH_LONG).show();
            return;
        }

        ExcluirContaRequest request = new ExcluirContaRequest(email, senha);

        btnExcluirConta.setEnabled(false);

        RetrofitClient.getApiService().excluirConta(request).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                btnExcluirConta.setEnabled(true);

                if (response.isSuccessful()) {

                    preferencias.edit().clear().apply();

                    Toast.makeText(ConfiguracoesActivity.this, "Conta excluída com sucesso.", Toast.LENGTH_LONG).show();

                    abrirLogin();

                } else if (response.code() == 401) {

                    Toast.makeText(ConfiguracoesActivity.this, "Senha incorreta.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(ConfiguracoesActivity.this, "Não foi possível excluir a conta.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                btnExcluirConta.setEnabled(true);

                Toast.makeText(ConfiguracoesActivity.this, "Erro ao conectar ao servidor.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void abrirLogin() {

        Intent intent = new Intent(ConfiguracoesActivity.this, LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}