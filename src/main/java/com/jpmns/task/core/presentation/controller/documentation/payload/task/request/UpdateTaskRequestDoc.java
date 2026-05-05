package com.jpmns.task.core.presentation.controller.documentation.payload.task.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateTaskRequest", description = "Dados para atualização do nome de uma tarefa existente")
public interface UpdateTaskRequestDoc {

    @Schema(description = "Novo nome da tarefa. Não pode ser vazio e deve ter no máximo 255 caracteres.", example = "Estudar Spring Boot avançado", maxLength = 255)
    String taskName();
}
