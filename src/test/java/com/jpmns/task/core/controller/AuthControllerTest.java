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
import com.jpmns.task.core.fixture.UserFixture;
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
            var user = UserFixture.aUser();
            var username = user.getUsername();
            var password = user.getPassword();
            var accessToken = "access-token";
            var refreshToken = "refresh-token";
            var output = new UserLoginOutputDTO(accessToken, refreshToken);

            when(userLoginUseCase.execute(any())).thenReturn(output);

            perform(username.asString(), password.asString())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(accessToken))
                    .andExpect(jsonPath("$.refreshToken").value(refreshToken));
        }

        @Test
        @DisplayName("Should return 401 when credentials are invalid")
        void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
            var user = UserFixture.aUser();
            var username = user.getUsername();
            var wrongPassword = "wrong-password";

            when(userLoginUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

            perform(username.asString(), wrongPassword)
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when username is blank")
        void shouldReturn400WhenUsernameIsBlank() throws Exception {
            var user = UserFixture.aUser();
            var password = user.getPassword();
            var emptyUsername = "";

            perform(emptyUsername, password.asString())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is blank")
        void shouldReturn400WhenPasswordIsBlank() throws Exception {
            var user = UserFixture.aUser();
            var username = user.getUsername();
            var emptyPassword = "";

            perform(username.asString(), emptyPassword)
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
            var accessToken = "access-token";
            var refreshToken = "refresh-token";
            var output = new RefreshUserTokenOutputDTO(accessToken, refreshToken);

            when(refreshUserTokenUseCase.execute(any())).thenReturn(output);

            perform(refreshToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(accessToken))
                    .andExpect(jsonPath("$.refreshToken").value(refreshToken));
        }

        @Test
        @DisplayName("Should return 401 when refresh token is invalid")
        void shouldReturn401WhenRefreshTokenIsInvalid() throws Exception {
            var invalidToken = "invalid-token";

            when(refreshUserTokenUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

            perform(invalidToken)
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when refresh token is blank")
        void shouldReturn400WhenRefreshTokenIsBlank() throws Exception {
            var emptyToken = "";

            perform(emptyToken)
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
