package com.jpmns.task.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.jpmns.task.integration.common.abstracts.IntegrationTestBase;
import com.jpmns.task.integration.common.sql.SqlCreateSeed;
import com.jpmns.task.shared.security.WithJwtTokenMock;

@DisplayName("User Integration Tests")
class UserIntegrationTest extends IntegrationTestBase {
    private static final String EXISTING_USERNAME = "john";
    private static final String PASSWORD = "password";

    @Nested
    @DisplayName("POST /api/v1/users")
    class CreateUser {

        @Test
        @DisplayName("Should return 201 with id and username when input is valid")
        void shouldReturn201WhenInputIsValid() throws Exception {
            var username = "username";

            perform(username, PASSWORD)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.username").value(username));
        }

        @Test
        @DisplayName("Should return 409 when username already exists")
        @SqlCreateSeed
        void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
            perform(EXISTING_USERNAME, PASSWORD)
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 when username is too short")
        void shouldReturn400WhenUsernameIsTooShort() throws Exception {
            var username = "ab";

            perform(username, PASSWORD)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is too short")
        void shouldReturn400WhenPasswordIsTooShort() throws Exception {
            var username = "username";
            var password = "ab";

            perform(username, password)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when username is blank")
        void shouldReturn400WhenUsernameIsBlank() throws Exception {
            var username = "";
            var password = "ab";

            perform(username, password)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is blank")
        void shouldReturn400WhenPasswordIsBlank() throws Exception {
            var username = "username";
            var password = "";

            perform(username, password)
                    .andExpect(status().isBadRequest());
        }

        private ResultActions perform(String username, String password) throws Exception {
            var requestBody = """
                    {"username": "%s", "password": "%s"}
                    """.formatted(username, password);

            return mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users")
    class DeleteUser {

        @Test
        @DisplayName("Should return 204 when user is authenticated")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn204WhenAuthenticated() throws Exception {
            perform()
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            perform()
                    .andExpect(status().isUnauthorized());
        }

        private ResultActions perform() throws Exception {
            return mockMvc.perform(delete("/api/v1/users"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/username")
    class UpdateUsername {

        @Test
        @DisplayName("Should return 200 with new username when input is valid")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn200WhenInputIsValid() throws Exception {
            var newUsername = "newusername";

            perform(newUsername)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(newUsername));
        }

        @Test
        @DisplayName("Should return 409 when new username is already taken")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn409WhenUsernameAlreadyTaken() throws Exception {
            perform(EXISTING_USERNAME)
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 when new username is too short")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn400WhenUsernameTooShort() throws Exception {
            var newUsername = "ab";

            perform(newUsername)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            var newUsername = "newusername";

            perform(newUsername)
                    .andExpect(status().isUnauthorized());
        }

        private ResultActions perform(String newUsername) throws Exception {
            var requestBody = """
                    {"newUsername": "%s"}
                    """.formatted(newUsername);

            return mockMvc.perform(patch("/api/v1/users/username")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/password")
    class UpdatePassword {

        @Test
        @DisplayName("Should return 204 when current password is correct")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn204WhenCurrentPasswordIsCorrect() throws Exception {
            var newPassword = "newpassword";

            perform(PASSWORD, newPassword)
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 401 when current password is wrong")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn401WhenCurrentPasswordIsWrong() throws Exception {
            var currentPassword = "wrongpassword";
            var newPassword = "newpassword";

            perform(currentPassword, newPassword)
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when new password is too short")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn400WhenNewPasswordTooShort() throws Exception {
            var newPassword = "ab";

            perform(PASSWORD, newPassword)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            var newPassword = "newpassword";

            perform(PASSWORD, newPassword)
                    .andExpect(status().isUnauthorized());
        }

        private ResultActions perform(String currentPassword, String newPassword) throws Exception {
            var requestBody = """
                    {"currentPassword": "%s", "newPassword": "%s"}
                    """.formatted(currentPassword, newPassword);

            return mockMvc.perform(patch("/api/v1/users/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }
    }
}
