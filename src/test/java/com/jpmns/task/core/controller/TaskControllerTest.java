package com.jpmns.task.core.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO;
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException;
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException;
import com.jpmns.task.core.application.usecase.task.interfaces.CreateTaskUseCase;
import com.jpmns.task.core.application.usecase.task.interfaces.DeleteTaskUseCase;
import com.jpmns.task.core.application.usecase.task.interfaces.ListTasksUseCase;
import com.jpmns.task.core.application.usecase.task.interfaces.MarkTaskAsFinishedUseCase;
import com.jpmns.task.core.application.usecase.task.interfaces.UpdateTaskUseCase;
import com.jpmns.task.core.domain.task.TaskEntity;
import com.jpmns.task.core.fixture.TaskFixture;
import com.jpmns.task.core.presentation.controller.TaskController;
import com.jpmns.task.core.presentation.controller.common.handler.GlobalExceptionHandler;
import com.jpmns.task.shared.security.WithJwtTokenMock;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateTaskUseCase createTaskUseCase;

    @MockitoBean
    private ListTasksUseCase listTasksUseCase;

    @MockitoBean
    private UpdateTaskUseCase updateTaskUseCase;

    @MockitoBean
    private DeleteTaskUseCase deleteTaskUseCase;

    @MockitoBean
    private MarkTaskAsFinishedUseCase markTaskAsFinishedUseCase;

    @MockitoBean
    private Token token;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private TaskOutputDTO buildTaskOutput(TaskEntity task) {
        var taskId = task.getId();
        var taskName = task.getTaskName();
        var finished = task.getFinished();
        var userId = task.getUserId();

        return new TaskOutputDTO(
                taskId.asString(),
                userId.asString(),
                taskName.asString(),
                finished,
                Instant.now()
        );
    }

    @Nested
    @DisplayName("POST /api/v1/tasks")
    class CreateTask {

        @Test
        @DisplayName("Should return 201 with task data when creation succeeds")
        @WithJwtTokenMock
        void shouldReturn201WhenTaskIsCreatedSuccessfully() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();
            var taskName = task.getTaskName();
            var taskFinished = task.getFinished();
            var userId = task.getUserId();
            var output = buildTaskOutput(task);

            when(createTaskUseCase.execute(any())).thenReturn(output);

            perform(taskName.asString())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(taskId.asString()))
                    .andExpect(jsonPath("$.userId").value(userId.asString()))
                    .andExpect(jsonPath("$.taskName").value(taskName.asString()))
                    .andExpect(jsonPath("$.finished").value(taskFinished));
        }

        @Test
        @DisplayName("Should return 401 when request has no token")
        void shouldReturn401WhenRequestHasNoToken() throws Exception {
            var task = TaskFixture.aTask();
            var taskName = task.getTaskName();

            perform(taskName.asString())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when task name is blank")
        @WithJwtTokenMock
        void shouldReturn400WhenTaskNameIsBlank() throws Exception {
            var emptyTaskName = "";

            perform(emptyTaskName)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when task name exceeds 255 characters")
        @WithJwtTokenMock
        void shouldReturn400WhenTaskNameExceedsMaxLength() throws Exception {
            var largeTaskName = "a".repeat(256);

            perform(largeTaskName)
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
        @DisplayName("Should return 200 with list of tasks when user is authenticated")
        @WithJwtTokenMock
        void shouldReturn200WithTaskListWhenAuthenticated() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();
            var taskName = task.getTaskName();
            var output = List.of(buildTaskOutput(task));

            when(listTasksUseCase.execute(any())).thenReturn(output);

            perform()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(taskId.asString()))
                    .andExpect(jsonPath("$[0].taskName").value(taskName.asString()));
        }

        @Test
        @DisplayName("Should return 200 with empty list when user has no tasks")
        @WithJwtTokenMock
        void shouldReturn200WithEmptyListWhenUserHasNoTasks() throws Exception {
            when(listTasksUseCase.execute(any())).thenReturn(List.of());

            perform()
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should return 401 when request has no token")
        void shouldReturn401WhenRequestHasNoToken() throws Exception {
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
        @DisplayName("Should return 200 with updated task when update succeeds")
        @WithJwtTokenMock
        void shouldReturn200WhenTaskIsUpdatedSuccessfully() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();
            var taskFinished = task.getFinished();
            var userId = task.getUserId();
            var updatedTaskName = "Updated task name";
            var output = new TaskOutputDTO(
                    taskId.asString(),
                    userId.asString(),
                    updatedTaskName,
                    taskFinished,
                    Instant.now()
            );

            when(updateTaskUseCase.execute(any())).thenReturn(output);

            perform(taskId.asString(), updatedTaskName)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(taskId.asString()))
                    .andExpect(jsonPath("$.taskName").value(updatedTaskName));
        }

        @Test
        @DisplayName("Should return 404 when task is not found")
        @WithJwtTokenMock
        void shouldReturn404WhenTaskIsNotFound() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();
            var updatedTaskName = "Updated task name";

            when(updateTaskUseCase.execute(any())).thenThrow(new TaskNotFoundException());

            perform(taskId.asString(), updatedTaskName)
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when user does not own the task")
        @WithJwtTokenMock
        void shouldReturn403WhenUserDoesNotOwnTheTask() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();
            var updatedTaskName = "Updated task name";

            when(updateTaskUseCase.execute(any())).thenThrow(new TaskAccessDeniedException());

            perform(taskId.asString(), updatedTaskName)
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 400 when task name is blank")
        @WithJwtTokenMock
        void shouldReturn400WhenTaskNameIsBlank() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();
            var emptyTaskName = "";

            perform(taskId.asString(), emptyTaskName)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when request has no token")
        void shouldReturn401WhenRequestHasNoToken() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();
            var updatedTaskName = "Updated task name";

            perform(taskId.asString(), updatedTaskName)
                    .andExpect(status().isUnauthorized());
        }

        private ResultActions perform(String taskId, String taskName) throws Exception {
            var requestBody = """
                    {"taskName": "%s"}
                    """.formatted(taskName);;

            return mockMvc.perform(put("/api/v1/tasks/{taskId}", taskId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/tasks/{taskId}")
    class DeleteTask {

        @Test
        @DisplayName("Should return 204 when task is deleted successfully")
        @WithJwtTokenMock
        void shouldReturn204WhenTaskIsDeletedSuccessfully() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();

            doNothing().when(deleteTaskUseCase).execute(any());

            perform(taskId.asString())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 when task is not found")
        @WithJwtTokenMock
        void shouldReturn404WhenTaskIsNotFound() throws Exception {
            doThrow(new TaskNotFoundException()).when(deleteTaskUseCase).execute(any());

            perform(UUID.randomUUID().toString())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when user does not own the task")
        @WithJwtTokenMock
        void shouldReturn403WhenUserDoesNotOwnTheTask() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();

            doThrow(new TaskAccessDeniedException()).when(deleteTaskUseCase).execute(any());

            perform(taskId.asString())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when request has no token")
        void shouldReturn401WhenRequestHasNoToken() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();

            perform(taskId.asString())
                    .andExpect(status().isUnauthorized());
        }

        private ResultActions perform(String taskId) throws Exception {
            return mockMvc.perform(delete("/api/v1/tasks/{taskId}", taskId));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/tasks/{taskId}/finish")
    class MarkTaskAsFinished {

        @Test
        @DisplayName("Should return 204 when task is marked as finished successfully")
        @WithJwtTokenMock
        void shouldReturn204WhenTaskIsMarkedAsFinishedSuccessfully() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();

            doNothing().when(markTaskAsFinishedUseCase).execute(any());

            perform(taskId.asString())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 when task is not found")
        @WithJwtTokenMock
        void shouldReturn404WhenTaskIsNotFound() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();

            doThrow(new TaskNotFoundException()).when(markTaskAsFinishedUseCase).execute(any());

            perform(taskId.asString())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when user does not own the task")
        @WithJwtTokenMock
        void shouldReturn403WhenUserDoesNotOwnTheTask() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();

            doThrow(new TaskAccessDeniedException()).when(markTaskAsFinishedUseCase).execute(any());

            perform(taskId.asString())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when request has no token")
        void shouldReturn401WhenRequestHasNoToken() throws Exception {
            var task = TaskFixture.aTask();
            var taskId = task.getId();

            perform(taskId.asString())
                    .andExpect(status().isUnauthorized());
        }

        private ResultActions perform(String taskId) throws Exception {
            return mockMvc.perform(patch("/api/v1/tasks/{taskId}/finish", taskId));
        }
    }
}
