package com.jpmns.task.core.presentation.controller.documentation.payload.task.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateTaskRequest", description = "Dados para criação de uma nova tarefa")
public interface CreateTaskRequestDoc {

    @Schema(
            description = "Nome da tarefa. Não pode ser vazio e deve ter no máximo 255 caracteres.",
            example = "Estudar Spring Boot",
            maxLength = 255)
    String taskName();
}
