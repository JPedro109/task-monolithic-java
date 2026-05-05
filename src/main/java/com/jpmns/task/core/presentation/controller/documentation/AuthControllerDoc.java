package com.jpmns.task.core.presentation.controller.documentation;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jpmns.task.core.presentation.controller.payload.user.request.RefreshTokenRequest;
import com.jpmns.task.core.presentation.controller.payload.user.request.UserLoginRequest;
import com.jpmns.task.core.presentation.controller.payload.user.response.RefreshTokenResponse;
import com.jpmns.task.core.presentation.controller.payload.user.response.UserLoginResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "Endpoints de autenticação — login e renovação de tokens JWT")
@RequestMapping("/api/v1/auth")
public interface AuthControllerDoc {

    @Operation(
            summary = "Login do usuário",
            description = """
                    <p>Autentica um usuário com <strong>username</strong> e <strong>password</strong>.</p>
                    <p>Em caso de sucesso, retorna um par de tokens JWT:</p>
                    <ul>
                        <li><code>accessToken</code> — token de curta duração usado nas requisições autenticadas</li>
                        <li><code>refreshToken</code> — token de longa duração usado para renovar o <code>accessToken</code></li>
                    </ul>
                    <p>Envie o <code>accessToken</code> no header <code>Authorization: Bearer &lt;token&gt;</code> em todas as rotas protegidas.</p>
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserLoginRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Credenciais válidas",
                                            value = """
                                                    {
                                                      "username": "joao_silva",
                                                      "password": "senha@123"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Campos em branco (inválido)",
                                            value = """
                                                    {
                                                      "username": "",
                                                      "password": ""
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserLoginResponse.class),
                            examples = @ExampleObject(
                                    name = "Tokens gerados",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvX3NpbHZhIn0.abc123",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvX3NpbHZhIn0.xyz789"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Campos obrigatórios ausentes ou em branco",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Erro de validação",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Bad Request",
                                              "status": 400,
                                              "detail": "username: must not be blank"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Credenciais incorretas",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Unauthorized",
                                              "status": 401,
                                              "detail": "Invalid username or password"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Erro interno",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Internal Server Error",
                                              "status": 500,
                                              "detail": "Internal server error"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<UserLoginResponse> login(@Valid @org.springframework.web.bind.annotation.RequestBody UserLoginRequest request);

    @Operation(
            summary = "Renovar tokens JWT",
            description = """
                    <p>Gera um novo par de tokens (<code>accessToken</code> + <code>refreshToken</code>) a partir de um <strong>refreshToken</strong> válido.</p>
                    <p>Use este endpoint quando o <code>accessToken</code> expirar, evitando que o usuário precise fazer login novamente.</p>
                    <blockquote>
                        <strong>Atenção:</strong> o <code>refreshToken</code> enviado é invalidado após o uso — utilize sempre o novo par retornado.
                    </blockquote>
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Refresh token válido",
                                            value = """
                                                    {
                                                      "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvX3NpbHZhIn0.xyz789"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Token em branco (inválido)",
                                            value = """
                                                    {
                                                      "refreshToken": ""
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens renovados com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenResponse.class),
                            examples = @ExampleObject(
                                    name = "Novos tokens",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvX3NpbHZhIn0.newAccess",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2FvX3NpbHZhIn0.newRefresh"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Campo refreshToken ausente ou em branco",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Erro de validação",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Bad Request",
                                              "status": 400,
                                              "detail": "refreshToken: must not be blank"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token inválido ou expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Token inválido",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Unauthorized",
                                              "status": 401,
                                              "detail": "Invalid or expired token"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Erro interno",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Internal Server Error",
                                              "status": 500,
                                              "detail": "Internal server error"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<RefreshTokenResponse> refresh(@Valid @org.springframework.web.bind.annotation.RequestBody RefreshTokenRequest request);
}
