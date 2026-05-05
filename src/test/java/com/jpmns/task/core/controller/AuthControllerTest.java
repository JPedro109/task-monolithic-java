package com.jpmns.task.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jpmns.task.configuration.security.SecurityConfig;
import com.jpmns.task.core.application.port.security.Token;
import com.jpmns.task.core.application.usecase.user.dto.output.RefreshUserTokenOutputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.UserLoginOutputDTO;
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException;
import com.jpmns.task.core.application.usecase.user.interfaces.RefreshUserTokenUseCase;
import com.jpmns.task.core.application.usecase.user.interfaces.UserLoginUseCase;
import com.jpmns.task.core.presentation.controller.AuthController;
import com.jpmns.task.core.presentation.controller.common.handler.GlobalExceptionHandler;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserLoginUseCase userLoginUseCase;

    @MockitoBean
    private RefreshUserTokenUseCase refreshUserTokenUseCase;

    @MockitoBean
    private Token token;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("Should return 200 with tokens when credentials are valid")
        void shouldReturn200WhenCredentialsAreValid() throws Exception {
            var output = new UserLoginOutputDTO("access-token-value", "refresh-token-value");
            when(userLoginUseCase.execute(any())).thenReturn(output);

            perform("john_doe", "secret123")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token-value"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token-value"));
        }

        @Test
        @DisplayName("Should return 401 when credentials are invalid")
        void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
            when(userLoginUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

            perform("john_doe", "wrong-password")
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when username is blank")
        void shouldReturn400WhenUsernameIsBlank() throws Exception {
            perform("", "secret123")
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is blank")
        void shouldReturn400WhenPasswordIsBlank() throws Exception {
            perform("john_doe", "")
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when body is missing")
        void shouldReturn4xxWhenBodyIsMissing() throws Exception {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        private ResultActions perform(String username, String password) throws Exception {
            var requestBody = """
                    {"username": "%s", "password": "%s"}
                    """.formatted(username, password);

            return mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("Should return 200 with new tokens when refresh token is valid")
        void shouldReturn200WhenRefreshTokenIsValid() throws Exception {
            var output = new RefreshUserTokenOutputDTO("new-access-token", "new-refresh-token");
            when(refreshUserTokenUseCase.execute(any())).thenReturn(output);

            perform("valid-refresh-token")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
        }

        @Test
        @DisplayName("Should return 401 when refresh token is invalid")
        void shouldReturn401WhenRefreshTokenIsInvalid() throws Exception {
            when(refreshUserTokenUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

            perform("invalid-token")
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when refresh token is blank")
        void shouldReturn400WhenRefreshTokenIsBlank() throws Exception {
            perform("")
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when body is missing")
        void shouldReturn4xxWhenBodyIsMissing() throws Exception {
            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        private ResultActions perform(String refreshToken) throws Exception {
            var requestBody = """
                    {"refreshToken": "%s"}
                    """.formatted(refreshToken);

            return mockMvc.perform(post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }
    }
}
