package com.jpmns.task.core.presentation.controller.payload.task.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.jpmns.task.core.presentation.controller.documentation.payload.task.request.CreateTaskRequestDoc;

public record CreateTaskRequest(
        @NotBlank @Size(max = 255) String taskName
) implements CreateTaskRequestDoc {
    @Override
    public String toString() {
        return "CreateTaskRequest{taskName='" + taskName + "'}";
    }
}
