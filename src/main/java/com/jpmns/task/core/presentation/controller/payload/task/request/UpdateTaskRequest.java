package com.jpmns.task.core.presentation.controller.payload.task.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.jpmns.task.core.presentation.controller.documentation.payload.task.request.UpdateTaskRequestDoc;

public record UpdateTaskRequest(
        @NotBlank @Size(max = 255) String taskName
) implements UpdateTaskRequestDoc {
    @Override
    public String toString() {
        return "UpdateTaskRequest{taskName='" + taskName + "'}";
    }
}
