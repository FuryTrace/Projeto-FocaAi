package com.example.focaai.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;
import com.example.focaai.api.RetrofitClient;
import com.example.focaai.api.dto.LoginRequest;
import com.example.focaai.api.dto.UsuarioResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editSenha;
    private Button buttonEntrar;
    private TextView textRecuperarSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editEmail = findViewById(R.id.editEmailLogin);
        editSenha = findViewById(R.id.editSenhaLogin);
        buttonEntrar = findViewById(R.id.buttonEntrar);
        textRecuperarSenha = findViewById(R.id.textRecuperarSenha);

        // Recuperar o e-mail informado após o cadastro
        String emailCadastro = getIntent().getStringExtra("email_cadastro");

        if (emailCadastro != null && !emailCadastro.isEmpty()) {
            editEmail.setText(emailCadastro);
        }

        // Botão de entrar
        buttonEntrar.setOnClickListener(v -> realizarLogin());

        // Abrir recuperação de senha
        textRecuperarSenha.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RecuperandoSenhaActivity.class);
            startActivity(intent);
        });
    }

    private void realizarLogin() {

        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString();

        // Validar campos vazios
        if (email.isEmpty()) {
            editEmail.setError("Informe seu e-mail.");
            editEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Digite um e-mail válido.");
            editEmail.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            editSenha.setError("Informe sua senha.");
            editSenha.requestFocus();
            return;
        }

        // Preparar os dados para a API
        LoginRequest request = new LoginRequest(email, senha);

        buttonEntrar.setEnabled(false);

        // Enviar login para o backend
        RetrofitClient.getApiService().loginUsuario(request).enqueue(new Callback<UsuarioResponse>() {

            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {

                buttonEntrar.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {

                    UsuarioResponse usuario = response.body();

                    // Salvar os dados básicos do usuário
                    SharedPreferences preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);

                    preferencias.edit()
                            .putLong("id_usuario", usuario.getId())
                            .putString("nome_usuario", usuario.getNome())
                            .putString("email_usuario", usuario.getEmail())
                            .apply();

                    Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                    // Abrir a tela principal
                    Intent intent = new Intent(LoginActivity.this, PrincipalActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);

                } else if (response.code() == 401) {

                    Toast.makeText(LoginActivity.this, "E-mail ou senha incorretos.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(LoginActivity.this, "Não foi possível realizar o login. Tente novamente.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {

                buttonEntrar.setEnabled(true);

                Toast.makeText(LoginActivity.this, "Não foi possível conectar ao servidor. Verifique se a API está funcionando.", Toast.LENGTH_LONG).show();
            }
        });
    }
}