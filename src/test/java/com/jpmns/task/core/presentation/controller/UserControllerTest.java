package com.jpmns.task.core.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import com.jpmns.task.core.application.usecase.user.dto.output.CreateUserOutputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.UpdateUsernameOutputDTO;
import com.jpmns.task.core.application.usecase.user.exception.InvalidCredentialsException;
import com.jpmns.task.core.application.usecase.user.exception.UserNotFoundException;
import com.jpmns.task.core.application.usecase.user.exception.UsernameAlreadyExistsException;
import com.jpmns.task.core.application.usecase.user.interfaces.CreateUserUseCase;
import com.jpmns.task.core.application.usecase.user.interfaces.DeleteUserUseCase;
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUserPasswordUseCase;
import com.jpmns.task.core.application.usecase.user.interfaces.UpdateUsernameUseCase;
import com.jpmns.task.core.presentation.controller.common.handler.GlobalExceptionHandler;
import com.jpmns.task.shared.fixture.UserFixture;
import com.jpmns.task.shared.security.WithJwtTokenMock;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;

    @MockitoBean
    private UpdateUserPasswordUseCase updateUserPasswordUseCase;

    @MockitoBean
    private UpdateUsernameUseCase updateUsernameUseCase;

    @MockitoBean
    private Token token;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("POST /api/v1/users")
    class CreateUser {

        @Test
        @DisplayName("Should return 201 with user data when creation succeeds")
        void shouldReturn201WhenUserIsCreatedSuccessfully() throws Exception {
            var user = UserFixture.aUser();
            var userId = user.getId();
            var username = user.getUsername();
            var password = user.getPassword();
            var output = new CreateUserOutputDTO(userId.asString(), username.asString());

            when(createUserUseCase.execute(any())).thenReturn(output);

            perform(username.asString(), password.asString())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(userId.asString()))
                    .andExpect(jsonPath("$.username").value(username.asString()));
        }

        @Test
        @DisplayName("Should return 409 when username already exists")
        void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
            var user = UserFixture.aUser();
            var username = user.getUsername();
            var password = user.getPassword();

            when(createUserUseCase.execute(any())).thenThrow(new UsernameAlreadyExistsException());

            perform(username.asString(), password.asString())
                    .andExpect(status().isConflict());
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
        @DisplayName("Should return 400 when username is shorter than 3 characters")
        void shouldReturn400WhenUsernameIsTooShort() throws Exception {
            var user = UserFixture.aUser();
            var password = user.getPassword();
            var shortUsername = "ab";

            perform(shortUsername, password.asString())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is shorter than 8 characters")
        void shouldReturn400WhenPasswordIsTooShort() throws Exception {
            var user = UserFixture.aUser();
            var username = user.getUsername();
            var shortPassword = "ab";

            perform(username.asString(), shortPassword)
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
        @DisplayName("Should return 204 when user is deleted successfully")
        @WithJwtTokenMock
        void shouldReturn204WhenUserIsDeletedSuccessfully() throws Exception {
            doNothing().when(deleteUserUseCase).execute(any());

            perform()
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 401 when request has no token")
        void shouldReturn401WhenRequestHasNoToken() throws Exception {
            perform()
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when user is not found")
        @WithJwtTokenMock
        void shouldReturn404WhenUserIsNotFound() throws Exception {
            doThrow(new UserNotFoundException()).when(deleteUserUseCase).execute(any());

            perform()
                    .andExpect(status().isNotFound());
        }

        private ResultActions perform() throws Exception {
            return mockMvc.perform(delete("/api/v1/users"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/password")
    class UpdatePassword {

        @Test
        @DisplayName("Should return 204 when password is updated successfully")
        @WithJwtTokenMock
        void shouldReturn204WhenPasswordIsUpdatedSuccessfully() throws Exception {
            var oldPassword = "old-password";
            var newPassword = "new-password";

            doNothing().when(updateUserPasswordUseCase).execute(any());

            perform(oldPassword, newPassword)
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 401 when current password is wrong")
        void shouldReturn401WhenCurrentPasswordIsWrong() throws Exception {
            var wrongOldPassword = "wrong-pass";
            var newPassword = "new-password";

            doThrow(new InvalidCredentialsException()).when(updateUserPasswordUseCase).execute(any());

            perform(wrongOldPassword, newPassword)
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when new password is shorter than 8 characters")
        @WithJwtTokenMock
        void shouldReturn400WhenNewPasswordIsTooShort() throws Exception {
            var oldPassword = "old-password";
            var newPassword = "ab";

            perform(oldPassword, newPassword)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when request has no token")
        void shouldReturn401WhenRequestHasNoToken() throws Exception {
            var oldPassword = "old-password";
            var newPassword = "new-password";

            perform(oldPassword, newPassword)
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

    @Nested
    @DisplayName("PATCH /api/v1/users/username")
    class UpdateUsername {

        @Test
        @DisplayName("Should return 200 with updated username when update succeeds")
        @WithJwtTokenMock
        void shouldReturn200WhenUsernameIsUpdatedSuccessfully() throws Exception {
            var user = UserFixture.aUser();
            var userId = user.getId();
            var username = user.getUsername();
            var output = new UpdateUsernameOutputDTO(userId.asString(), username.asString());

            when(updateUsernameUseCase.execute(any())).thenReturn(output);

            perform(username.asString())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId.asString()))
                    .andExpect(jsonPath("$.username").value(username.asString()));
        }

        @Test
        @DisplayName("Should return 409 when new username already exists")
        @WithJwtTokenMock
        void shouldReturn409WhenNewUsernameAlreadyExists() throws Exception {
            var user = UserFixture.aUser();
            var username = user.getUsername();

            when(updateUsernameUseCase.execute(any())).thenThrow(new UsernameAlreadyExistsException());

            perform(username.asString())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 when new username is shorter than 3 characters")
        @WithJwtTokenMock
        void shouldReturn400WhenNewUsernameIsTooShort() throws Exception {
            var shortNewUsername = "ab";

            perform(shortNewUsername)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when request has no token")
        void shouldReturn401WhenRequestHasNoToken() throws Exception {
            var user = UserFixture.aUser();
            var username = user.getUsername();

            perform(username.asString())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 404 when user is not found")
        @WithJwtTokenMock
        void shouldReturn404WhenUserIsNotFound() throws Exception {
            var user = UserFixture.aUser();
            var username = user.getUsername();

            when(updateUsernameUseCase.execute(any())).thenThrow(new UserNotFoundException());

            perform(username.asString())
                    .andExpect(status().isNotFound());
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
}
