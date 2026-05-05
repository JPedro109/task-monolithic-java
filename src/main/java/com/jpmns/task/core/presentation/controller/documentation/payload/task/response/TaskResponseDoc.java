package com.jpmns.task.core.presentation.controller.documentation.payload.task.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TaskResponse", description = "Dados de uma tarefa")
public interface TaskResponseDoc {

    @Schema(description = "Identificador único da tarefa (UUID)", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    String id();

    @Schema(description = "Identificador do usuário dono da tarefa (UUID)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    String userId();

    @Schema(description = "Nome da tarefa", example = "Estudar Spring Boot")
    String taskName();

    @Schema(description = "Indica se a tarefa foi concluída", example = "false")
    boolean finished();

    @Schema(description = "Data e hora de criação da tarefa no formato ISO-8601 (UTC)", example = "2026-04-26T10:00:00Z")
    Instant createdAt();
}
