package com.jpmns.task.core.presentation.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jpmns.task.core.application.usecase.task.dto.input.CreateTaskInputDTO;
import com.jpmns.task.core.application.usecase.task.dto.input.DeleteTaskInputDTO;
import com.jpmns.task.core.application.usecase.task.dto.input.ListTasksInputDTO;
import com.jpmns.task.core.application.usecase.task.dto.input.MarkTaskAsFinishedInputDTO;
import com.jpmns.task.core.application.usecase.task.dto.input.UpdateTaskInputDTO;
import com.jpmns.task.core.application.usecase.task.interfaces.CreateTaskUseCase;
import com.jpmns.task.core.application.usecase.task.interfaces.DeleteTaskUseCase;
import com.jpmns.task.core.application.usecase.task.interfaces.ListTasksUseCase;
import com.jpmns.task.core.application.usecase.task.interfaces.MarkTaskAsFinishedUseCase;
import com.jpmns.task.core.application.usecase.task.interfaces.UpdateTaskUseCase;
import com.jpmns.task.core.presentation.controller.common.resolver.AuthenticatedUserResolver;
import com.jpmns.task.core.presentation.controller.documentation.TaskControllerDoc;
import com.jpmns.task.core.presentation.controller.payload.task.request.CreateTaskRequest;
import com.jpmns.task.core.presentation.controller.payload.task.request.UpdateTaskRequest;
import com.jpmns.task.core.presentation.controller.payload.task.response.TaskResponse;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController implements TaskControllerDoc {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

    private final CreateTaskUseCase createTaskUseCase;
    private final ListTasksUseCase listTasksUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final MarkTaskAsFinishedUseCase markTaskAsFinishedUseCase;

    public TaskController(CreateTaskUseCase createTaskUseCase,
                          ListTasksUseCase listTasksUseCase,
                          UpdateTaskUseCase updateTaskUseCase,
                          DeleteTaskUseCase deleteTaskUseCase,
                          MarkTaskAsFinishedUseCase markTaskAsFinishedUseCase) {
        this.createTaskUseCase = createTaskUseCase;
        this.listTasksUseCase = listTasksUseCase;
        this.updateTaskUseCase = updateTaskUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.markTaskAsFinishedUseCase = markTaskAsFinishedUseCase;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        LOGGER.info("Creating task - request: {}", request);

        var userId = AuthenticatedUserResolver.getUserId();

        var dto = new CreateTaskInputDTO(userId, request.taskName());
        var output = createTaskUseCase.execute(dto);

        var response = TaskResponse.of(output);

        LOGGER.info("Creating task - response: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks() {
        LOGGER.info("Listing tasks");

        var userId = AuthenticatedUserResolver.getUserId();

        var dto = new ListTasksInputDTO(userId);
        var output = listTasksUseCase.execute(dto)
                .stream()
                .map(TaskResponse::of)
                .toList();

        LOGGER.info("Listing tasks - count={}", output.size());
        return ResponseEntity.ok(output);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable String taskId,
                                                   @Valid @RequestBody UpdateTaskRequest request) {
        LOGGER.info("Updating task - taskId={}, request: {}", taskId, request);

        var userId = AuthenticatedUserResolver.getUserId();

        var dto = new UpdateTaskInputDTO(taskId, userId, request.taskName());
        var output = updateTaskUseCase.execute(dto);

        var response = TaskResponse.of(output);

        LOGGER.info("Updating task - taskId={}, response: {}", taskId, response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        LOGGER.info("Deleting task - task id={}", taskId);

        var userId = AuthenticatedUserResolver.getUserId();

        var dto = new DeleteTaskInputDTO(taskId, userId);
        deleteTaskUseCase.execute(dto);

        LOGGER.info("Task deleted successfully - task id={}", taskId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/finish")
    public ResponseEntity<Void> markAsFinished(@PathVariable String taskId) {
        LOGGER.info("Marking task as finished - task id={}", taskId);

        var userId = AuthenticatedUserResolver.getUserId();

        var dto = new MarkTaskAsFinishedInputDTO(taskId, userId);
        markTaskAsFinishedUseCase.execute(dto);

        LOGGER.info("Task marked as finished successfully - task id={}", taskId);
        return ResponseEntity.noContent().build();
    }
}
