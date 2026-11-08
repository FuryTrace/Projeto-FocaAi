package com.example.focaai.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.focaai.R;
import com.example.focaai.api.RetrofitClient;
import com.example.focaai.api.dto.CadastroRequest;
import com.example.focaai.api.dto.UsuarioResponse;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroActivity extends AppCompatActivity {

    private EditText editNome;
    private EditText editEmail;
    private EditText editSenha;
    private EditText editConfirmarSenha;
    private Button buttonCriarConta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editNome = findViewById(R.id.editNomeCadastro);
        editEmail = findViewById(R.id.editEmailCadastro);
        editSenha = findViewById(R.id.editSenhaCadastro);
        editConfirmarSenha = findViewById(R.id.editConfirmarSenhaCadastro);
        buttonCriarConta = findViewById(R.id.buttonCriarConta);

        buttonCriarConta.setOnClickListener(v -> cadastrarUsuario());
    }

    private void cadastrarUsuario() {

        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString();
        String confirmarSenha = editConfirmarSenha.getText().toString();

        // Validar campos vazios
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {

            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar e-mail
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Digite um e-mail válido.");
            editEmail.requestFocus();
            return;
        }

        // Validar confirmação da senha
        if (!senha.equals(confirmarSenha)) {
            editConfirmarSenha.setError("As senhas não coincidem.");
            editConfirmarSenha.requestFocus();
            return;
        }

        // Preparar os dados para enviar à API
        CadastroRequest request = new CadastroRequest(nome, email, senha);

        buttonCriarConta.setEnabled(false);

        // Enviar cadastro para o backend
        RetrofitClient.getApiService().cadastrarUsuario(request).enqueue(new Callback<UsuarioResponse>() {

            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {

                buttonCriarConta.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {

                    UsuarioResponse usuario = response.body();

                    Toast.makeText(CadastroActivity.this, "Conta criada com sucesso!", Toast.LENGTH_LONG).show();

                    // Abrir a tela de login e preencher o e-mail
                    Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);

                    intent.putExtra("email_cadastro", usuario.getEmail());

                    startActivity(intent);
                    finish();

                } else {

                    String mensagem = "Não foi possível realizar o cadastro.";

                    try {
                        if (response.errorBody() != null) {
                            String erro = response.errorBody().string();

                            if (erro.contains("já cadastrado") || erro.contains("already") || erro.contains("exists")) {
                                mensagem = "Este e-mail já está cadastrado.";
                            }
                        }
                    } catch (IOException e) {
                        // Mantém a mensagem padrão
                    }

                    Toast.makeText(CadastroActivity.this, mensagem, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {

                buttonCriarConta.setEnabled(true);

                Toast.makeText(CadastroActivity.this, "Não foi possível conectar ao servidor. Verifique se a API está funcionando.", Toast.LENGTH_LONG).show();
            }
        });
    }
}