package com.example.focaai.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.focaai.api.dto.ValidarCodigoRequest;
import com.example.focaai.api.dto.TokenRecuperacaoResponse;
import com.example.focaai.api.dto.RedefinirSenhaRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecuperarSenhaActivity extends AppCompatActivity {

    private EditText editCodigo;
    private EditText editNovaSenha;
    private EditText editConfirmarNovaSenha;
    private Button buttonSalvar;

    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar_senha);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editCodigo = findViewById(R.id.editCodigoRecuperacao);
        editNovaSenha = findViewById(R.id.editNovaSenha);
        editConfirmarNovaSenha = findViewById(R.id.editConfirmarNovaSenha);
        buttonSalvar = findViewById(R.id.buttonSalvarNovaSenha);

        email = getIntent().getStringExtra("email_recuperacao");

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "E-mail de recuperação não informado.", Toast.LENGTH_LONG).show();

            finish();
            return;
        }

        buttonSalvar.setOnClickListener(v -> validarCodigo());
    }

    private void validarCodigo() {

        String codigo = editCodigo.getText().toString().trim();
        String novaSenha = editNovaSenha.getText().toString();
        String confirmarSenha = editConfirmarNovaSenha.getText().toString();

        if (codigo.length() != 6) {
            editCodigo.setError("Informe o código de 6 dígitos.");
            editCodigo.requestFocus();
            return;
        }

        if (novaSenha.isEmpty()) {
            editNovaSenha.setError("Informe a nova senha.");
            editNovaSenha.requestFocus();
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            editConfirmarNovaSenha.setError("As senhas não coincidem.");
            editConfirmarNovaSenha.requestFocus();
            return;
        }

        ValidarCodigoRequest request = new ValidarCodigoRequest(email, codigo);

        buttonSalvar.setEnabled(false);

        RetrofitClient.getApiService().validarCodigo(request).enqueue(new Callback<TokenRecuperacaoResponse>() {

                    @Override
                    public void onResponse(Call<TokenRecuperacaoResponse> call, Response<TokenRecuperacaoResponse> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().getToken() != null) {

                            String token = response.body().getToken();

                            redefinirSenha(token, novaSenha);

                        } else {

                            buttonSalvar.setEnabled(true);

                            Toast.makeText(RecuperarSenhaActivity.this, "Código inválido ou expirado. Solicite outro código.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<TokenRecuperacaoResponse> call, Throwable t) {

                        buttonSalvar.setEnabled(true);

                        Toast.makeText(RecuperarSenhaActivity.this, "Não foi possível validar o código. Verifique sua conexão.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void redefinirSenha(String token, String novaSenha) {

        RedefinirSenhaRequest request = new RedefinirSenhaRequest(email, token, novaSenha);

        RetrofitClient.getApiService().redefinirSenha(request).enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                        buttonSalvar.setEnabled(true);

                        if (response.isSuccessful()) {

                            Toast.makeText(RecuperarSenhaActivity.this, "Senha alterada com sucesso!", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(RecuperarSenhaActivity.this, LoginActivity.class);

                            intent.putExtra("email_cadastro", email);

                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);

                        } else {

                            Toast.makeText(RecuperarSenhaActivity.this, "Não foi possível alterar a senha. Tente novamente.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                        buttonSalvar.setEnabled(true);

                        Toast.makeText(RecuperarSenhaActivity.this, "Erro ao conectar ao servidor.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}