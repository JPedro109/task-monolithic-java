package com.jpmns.task.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmns.task.integration.common.abstracts.IntegrationTestBase;
import com.jpmns.task.integration.common.sql.SqlCreateSeed;

@DisplayName("Auth Integration Tests")
@Import(ObjectMapper.class)
class AuthIntegrationTest extends IntegrationTestBase {
    @Autowired
    protected ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("Should return 200 with access and refresh tokens when credentials are valid")
        @SqlCreateSeed
        void shouldReturn200WhenCredentialsAreValid() throws Exception {
            perform("john", "password")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("Should return 401 when password is wrong")
        void shouldReturn401WhenPasswordIsWrong() throws Exception {
            perform("wrongusername", "wrongpassword")
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 when user does not exist")
        void shouldReturn401WhenUserDoesNotExist() throws Exception {
            perform("nonexistentuser", "password")
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when username is blank")
        void shouldReturn400WhenUsernameIsBlank() throws Exception {
            perform("", "password123")
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is blank")
        void shouldReturn400WhenPasswordIsBlank() throws Exception {
            perform("john", "")
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
        @DisplayName("Should return 200 with new access token when refresh token is valid")
        @SqlCreateSeed
        void shouldReturn200WhenRefreshTokenIsValid() throws Exception {
            var loginBody = """
                    {"username": "john", "password": "password"}
                    """;
            var response = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginBody))
                    .andExpect(status().isOk())
                    .andReturn();
            var json = objectMapper.readTree(response.getResponse().getContentAsString());
            var refreshToken = json.get("refreshToken").asText();

            perform(refreshToken)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty());
        }

        @Test
        @DisplayName("Should return 401 when refresh token is invalid")
        void shouldReturn401WhenRefreshTokenIsInvalid() throws Exception {
            perform("invalidrefreshtoken")
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when refresh token is blank")
        void shouldReturn400WhenRefreshTokenIsBlank() throws Exception {
            perform("")
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
