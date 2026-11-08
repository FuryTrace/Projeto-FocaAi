package com.example.focaai.api;

import com.example.focaai.api.dto.CadastroRequest;
import com.example.focaai.api.dto.LoginRequest;
import com.example.focaai.api.dto.UsuarioResponse;
import com.example.focaai.api.dto.SolicitarRecuperacaoRequest;
import com.example.focaai.api.dto.ValidarCodigoRequest;
import com.example.focaai.api.dto.TokenRecuperacaoResponse;
import com.example.focaai.api.dto.RedefinirSenhaRequest;
import com.example.focaai.api.dto.ExcluirContaRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/usuarios/cadastro")
    Call<UsuarioResponse> cadastrarUsuario(@Body CadastroRequest request);

    @POST("api/usuarios/login")
    Call<UsuarioResponse> loginUsuario(@Body LoginRequest request);

    @POST("api/usuarios/recuperacao/solicitar")
    Call<Void> solicitarRecuperacao(@Body SolicitarRecuperacaoRequest request);

    @POST("api/usuarios/recuperacao/validar")
    Call<TokenRecuperacaoResponse> validarCodigo(@Body ValidarCodigoRequest request);

    @POST("api/usuarios/recuperacao/redefinir")
    Call<Void> redefinirSenha(@Body RedefinirSenhaRequest request);

    @POST("api/usuarios/excluir")
    Call<Void> excluirConta(@Body ExcluirContaRequest request);
}