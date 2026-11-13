package com.example.focaai.activities;

import android.content.SharedPreferences;
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
import com.example.focaai.api.dto.AlterarSenhaRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlterarSenhaActivity extends AppCompatActivity {

    private EditText edtSenhaAtual;
    private EditText edtNovaSenha;
    private EditText edtConfirmarSenha;
    private Button btnSalvar;
    private SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alterar_senha);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top,
                    systemBars.right, systemBars.bottom);
            return insets;
        });

        preferencias = getSharedPreferences("FocaAi", MODE_PRIVATE);

        edtSenhaAtual = findViewById(R.id.edtSenhaAtual);
        edtNovaSenha = findViewById(R.id.edtNovaSenha);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarNovaSenha);
        btnSalvar = findViewById(R.id.btnSalvarNovaSenha);

        findViewById(R.id.btnVoltarAlterarSenha).setOnClickListener(v -> finish());
        btnSalvar.setOnClickListener(v -> validarEAlterarSenha());
    }

    private void validarEAlterarSenha() {
        String atual = edtSenhaAtual.getText().toString();
        String nova = edtNovaSenha.getText().toString();
        String confirmacao = edtConfirmarSenha.getText().toString();
        String email = preferencias.getString("email_usuario", "");

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Não foi possível identificar sua conta. Faça login novamente.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (atual.isEmpty()) {
            edtSenhaAtual.setError("Informe sua senha atual.");
            edtSenhaAtual.requestFocus();
            return;
        }

        if (nova.length() < 8) {
            edtNovaSenha.setError("A nova senha deve ter pelo menos 8 caracteres.");
            edtNovaSenha.requestFocus();
            return;
        }

        if (!nova.equals(confirmacao)) {
            edtConfirmarSenha.setError("As senhas não coincidem.");
            edtConfirmarSenha.requestFocus();
            return;
        }

        if (atual.equals(nova)) {
            edtNovaSenha.setError("A nova senha deve ser diferente da atual.");
            edtNovaSenha.requestFocus();
            return;
        }

        AlterarSenhaRequest request = new AlterarSenhaRequest(email, atual, nova);
        btnSalvar.setEnabled(false);

        RetrofitClient.getApiService().alterarSenha(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnSalvar.setEnabled(true);

                if (response.isSuccessful()) {
                    edtSenhaAtual.setText("");
                    edtNovaSenha.setText("");
                    edtConfirmarSenha.setText("");

                    Toast.makeText(AlterarSenhaActivity.this,
                            "Senha alterada com sucesso!", Toast.LENGTH_LONG).show();
                    finish();

                } else if (response.code() == 401) {
                    edtSenhaAtual.setError("A senha atual está incorreta.");
                    edtSenhaAtual.requestFocus();

                } else if (response.code() == 400) {
                    Toast.makeText(AlterarSenhaActivity.this,
                            "Dados inválidos. Verifique a nova senha.", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(AlterarSenhaActivity.this,
                            "Não foi possível alterar a senha. Tente novamente.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSalvar.setEnabled(true);
                Toast.makeText(AlterarSenhaActivity.this,
                        "Erro ao conectar ao servidor. Verifique sua conexão.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
