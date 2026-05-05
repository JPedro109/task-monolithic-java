package com.jpmns.task.core.presentation.controller.payload.task.response;

import java.time.Instant;

import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO;
import com.jpmns.task.core.presentation.controller.documentation.payload.task.response.TaskResponseDoc;

public record TaskResponse(
        String id,
        String userId,
        String taskName,
        boolean finished,
        Instant createdAt
) implements TaskResponseDoc {

    public static TaskResponse of(TaskOutputDTO dto) {
        return new TaskResponse(dto.id(), dto.userId(), dto.taskName(), dto.finished(), dto.createdAt());
    }
}
