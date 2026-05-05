package com.jpmns.task.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.jpmns.task.integration.common.abstracts.IntegrationTestBase;
import com.jpmns.task.integration.common.sql.SqlCreateSeed;
import com.jpmns.task.shared.security.WithJwtTokenMock;

@DisplayName("Task Integration Tests")
class TaskIntegrationTest extends IntegrationTestBase {

    private static final String EXISTING_TASK_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901";
    private static final String USER_ID_WITHOUT_TASK = "41a385a3-de9f-44bb-ac0f-7a9fd6ac11e1";

    @Nested
    @DisplayName("POST /api/v1/tasks")
    class CreateTask {

        @Test
        @DisplayName("Should return 201 with task data when input is valid")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn201WhenInputIsValid() throws Exception {
            var taskName = "My first task";

            perform("My first task")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.taskName").value(taskName))
                    .andExpect(jsonPath("$.finished").value(false))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty());
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            var taskName = "My first task";

            perform(taskName)
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when taskName is blank")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn400WhenTaskNameIsBlank() throws Exception {
            var taskName = "";

            perform(taskName)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when taskName exceeds 255 characters")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn400WhenTaskNameTooLong() throws Exception {
            var taskName = "a".repeat(256);

            perform(taskName)
                    .andExpect(status().isBadRequest());
        }

        private ResultActions perform(String taskName) throws Exception {
            var requestBody = """
                    {"taskName": "%s"}
                    """.formatted(taskName);

            return mockMvc.perform(post("/api/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tasks")
    class ListTasks {

        @Test
        @DisplayName("Should return 200 with all tasks belonging to the authenticated user")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturnOnlyAuthenticatedUserTasks() throws Exception {
            perform()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].taskName").value("Buy groceries"));
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            perform()
                    .andExpect(status().isUnauthorized());
        }

        private ResultActions perform() throws Exception {
            return mockMvc.perform(get("/api/v1/tasks"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/tasks/{taskId}")
    class UpdateTask {

        @Test
        @DisplayName("Should return 200 with updated task name when input is valid")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn200WhenInputIsValid() throws Exception {
            var taskName = "Updated name";

            perform(EXISTING_TASK_ID, "Updated name")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(EXISTING_TASK_ID))
                    .andExpect(jsonPath("$.taskName").value(taskName));
        }

        @Test
        @DisplayName("Should return 403 when user does not own the task")
        @SqlCreateSeed
        @WithJwtTokenMock(sub = USER_ID_WITHOUT_TASK)
        void shouldReturn403WhenUserDoesNotOwnTask() throws Exception {
            var taskName = "Updated name";

            perform(EXISTING_TASK_ID, taskName)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when task does not exist")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn404WhenTaskNotFound() throws Exception {
            var taskId = UUID.randomUUID().toString();
            var taskName = "Updated name";

            perform(taskId, taskName)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when taskName is blank")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn400WhenTaskNameIsBlank() throws Exception {
            var taskName = "";

            perform(EXISTING_TASK_ID, taskName)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            var taskName = "";

            perform(EXISTING_TASK_ID, taskName)
                    .andExpect(status().isUnauthorized());
        }

        private ResultActions perform(String taskId, String taskName) throws Exception {
            var requestBody = """
                    {"taskName": "%s"}
                    """.formatted(taskName);

            return mockMvc.perform(put("/api/v1/tasks/" + taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/tasks/{taskId}")
    class DeleteTask {

        @Test
        @DisplayName("Should return 204 when user owns the task")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn204WhenUserOwnsTask() throws Exception {
            perform(EXISTING_TASK_ID)
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/tasks"))
                    .andExpect(jsonPath("$[?(@.id == '" + EXISTING_TASK_ID + "')]").doesNotExist());
        }

        @Test
        @DisplayName("Should return 403 when user does not own the task")
        @SqlCreateSeed
        @WithJwtTokenMock(sub = USER_ID_WITHOUT_TASK)
        void shouldReturn403WhenUserDoesNotOwnTask() throws Exception {
            perform(EXISTING_TASK_ID)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when task does not exist")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn404WhenTaskNotFound() throws Exception {
            var taskId = UUID.randomUUID().toString();

            perform(taskId)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            perform(EXISTING_TASK_ID)
                    .andExpect(status().isUnauthorized());
        }

        private ResultActions perform(String taskId) throws Exception {
            return mockMvc.perform(delete("/api/v1/tasks/" + taskId));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/tasks/{taskId}/finish")
    class MarkTaskAsFinished {

        @Test
        @DisplayName("Should return 204 and task should be marked as finished")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn204AndTaskShouldBeFinished() throws Exception {
            perform(EXISTING_TASK_ID)
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/tasks"))
                    .andExpect(jsonPath("$[?(@.id == '" + EXISTING_TASK_ID + "')].finished").value(true));
        }

        @Test
        @DisplayName("Should return 403 when user does not own the task")
        @SqlCreateSeed
        @WithJwtTokenMock(sub = USER_ID_WITHOUT_TASK)
        void shouldReturn403WhenUserDoesNotOwnTask() throws Exception {
            perform(EXISTING_TASK_ID)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when task does not exist")
        @SqlCreateSeed
        @WithJwtTokenMock
        void shouldReturn404WhenTaskNotFound() throws Exception {
            var taskId = UUID.randomUUID().toString();

            perform(taskId)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void shouldReturn401WhenNoToken() throws Exception {
            perform(EXISTING_TASK_ID)
                    .andExpect(status().isUnauthorized());
        }

        private ResultActions perform(String taskId) throws Exception {
            return mockMvc.perform(patch("/api/v1/tasks/" + taskId + "/finish"));
        }
    }
}
