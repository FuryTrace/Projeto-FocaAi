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
import com.example.focaai.api.dto.SolicitarRecuperacaoRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecuperandoSenhaActivity extends AppCompatActivity {

    private EditText editEmail;
    private Button buttonEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperando_senha);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editEmail = findViewById(R.id.editEmailRecuperacao);
        buttonEnviar = findViewById(R.id.buttonEnviarRecuperacao);

        buttonEnviar.setOnClickListener(v -> solicitarCodigo());
    }

    private void solicitarCodigo() {

        String email = editEmail.getText().toString().trim();

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

        SolicitarRecuperacaoRequest request =
                new SolicitarRecuperacaoRequest(email);

        buttonEnviar.setEnabled(false);

        RetrofitClient.getApiService().solicitarRecuperacao(request).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                buttonEnviar.setEnabled(true);

                if (response.isSuccessful()) {

                    Toast.makeText(RecuperandoSenhaActivity.this, "Solicitação realizada. Verifique seu e-mail.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(RecuperandoSenhaActivity.this, RecuperarSenhaActivity.class);

                    intent.putExtra("email_recuperacao", email);

                    startActivity(intent);

                } else {

                    Toast.makeText(RecuperandoSenhaActivity.this, "Não foi possível solicitar a recuperação. Verifique o e-mail e tente novamente.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

                buttonEnviar.setEnabled(true);

                Toast.makeText(RecuperandoSenhaActivity.this, "Não foi possível conectar ao servidor.", Toast.LENGTH_LONG).show();
            }
        });
    }
}