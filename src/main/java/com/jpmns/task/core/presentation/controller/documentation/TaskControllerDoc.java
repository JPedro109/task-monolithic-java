package com.jpmns.task.core.presentation.controller.documentation;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jpmns.task.core.presentation.controller.payload.task.request.CreateTaskRequest;
import com.jpmns.task.core.presentation.controller.payload.task.request.UpdateTaskRequest;
import com.jpmns.task.core.presentation.controller.payload.task.response.TaskResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Tasks", description = "Gerenciamento de tarefas — criação, listagem, atualização, exclusão e conclusão")
@RequestMapping("/api/v1/tasks")
@SecurityRequirement(name = "bearerAuth")
public interface TaskControllerDoc {

    @Operation(
            summary = "Criar nova tarefa",
            description = """
                    <p>Cria uma nova tarefa associada ao usuário autenticado.</p>
                    <p>A tarefa é criada com o status <code>finished: false</code> por padrão.</p>
                    <p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateTaskRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Tarefa válida",
                                            value = """
                                                    {
                                                      "taskName": "Estudar Spring Boot"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Nome em branco (inválido)",
                                            value = """
                                                    {
                                                      "taskName": ""
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Tarefa criada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class),
                            examples = @ExampleObject(
                                    name = "Tarefa criada",
                                    value = """
                                            {
                                              "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
                                              "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                              "taskName": "Estudar Spring Boot",
                                              "finished": false,
                                              "createdAt": "2026-04-26T10:00:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Nome em branco",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Bad Request",
                                              "status": 400,
                                              "detail": "taskName: must not be blank"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido ou expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Não autenticado",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Unauthorized",
                                              "status": 401,
                                              "detail": "Invalid or expired token"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Erro interno",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Internal Server Error",
                                              "status": 500,
                                              "detail": "Internal server error"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<TaskResponse> createTask(@Valid @org.springframework.web.bind.annotation.RequestBody CreateTaskRequest request);

    @Operation(
            summary = "Listar tarefas do usuário autenticado",
            description = """
                    <p>Retorna todas as tarefas pertencentes ao usuário autenticado.</p>
                    <p>A lista pode estar vazia caso o usuário ainda não tenha criado nenhuma tarefa.</p>
                    <p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de tarefas retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Lista com tarefas",
                                            value = """
                                                    [
                                                      {
                                                        "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
                                                        "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                        "taskName": "Estudar Spring Boot",
                                                        "finished": false,
                                                        "createdAt": "2026-04-26T10:00:00Z"
                                                      },
                                                      {
                                                        "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
                                                        "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                        "taskName": "Revisar documentação",
                                                        "finished": true,
                                                        "createdAt": "2026-04-25T08:30:00Z"
                                                      }
                                                    ]
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Lista vazia",
                                            value = "[]"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido ou expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Não autenticado",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Unauthorized",
                                              "status": 401,
                                              "detail": "Invalid or expired token"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Erro interno",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Internal Server Error",
                                              "status": 500,
                                              "detail": "Internal server error"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<List<TaskResponse>> listTasks();

    @Operation(
            summary = "Atualizar nome de uma tarefa",
            description = """
                    <p>Atualiza o <strong>nome</strong> de uma tarefa existente pertencente ao usuário autenticado.</p>
                    <p>Somente o dono da tarefa pode atualizá-la.</p>
                    <p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>
                    """,
            parameters = @Parameter(
                    name = "taskId",
                    description = "UUID da tarefa a ser atualizada",
                    required = true,
                    example = "b2c3d4e5-f6a7-8901-bcde-f12345678901"
            ),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateTaskRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Novo nome válido",
                                            value = """
                                                    {
                                                      "taskName": "Estudar Spring Boot avançado"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Nome em branco (inválido)",
                                            value = """
                                                    {
                                                      "taskName": ""
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarefa atualizada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponse.class),
                            examples = @ExampleObject(
                                    name = "Tarefa atualizada",
                                    value = """
                                            {
                                              "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
                                              "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                              "taskName": "Estudar Spring Boot avançado",
                                              "finished": false,
                                              "createdAt": "2026-04-26T10:00:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Nome em branco",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Bad Request",
                                              "status": 400,
                                              "detail": "taskName: must not be blank"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido ou expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Não autenticado",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Unauthorized",
                                              "status": 401,
                                              "detail": "Invalid or expired token"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não tem permissão para atualizar esta tarefa",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Acesso negado",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Forbidden",
                                              "status": 403,
                                              "detail": "Access denied to this task"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tarefa não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Tarefa não encontrada",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Not Found",
                                              "status": 404,
                                              "detail": "Task not found"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Erro interno",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Internal Server Error",
                                              "status": 500,
                                              "detail": "Internal server error"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<TaskResponse> updateTask(
            @PathVariable String taskId,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateTaskRequest request
    );

    @Operation(
            summary = "Excluir uma tarefa",
            description = """
                    <p>Remove permanentemente uma tarefa pertencente ao usuário autenticado.</p>
                    <p><strong>Esta operação é irreversível.</strong></p>
                    <p>Somente o dono da tarefa pode excluí-la.</p>
                    <p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>
                    """,
            parameters = @Parameter(
                    name = "taskId",
                    description = "UUID da tarefa a ser excluída",
                    required = true,
                    example = "b2c3d4e5-f6a7-8901-bcde-f12345678901"
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Tarefa excluída com sucesso — sem corpo de resposta"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido ou expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Não autenticado",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Unauthorized",
                                              "status": 401,
                                              "detail": "Invalid or expired token"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não tem permissão para excluir esta tarefa",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Acesso negado",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Forbidden",
                                              "status": 403,
                                              "detail": "Access denied to this task"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tarefa não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Tarefa não encontrada",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Not Found",
                                              "status": 404,
                                              "detail": "Task not found"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Erro interno",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Internal Server Error",
                                              "status": 500,
                                              "detail": "Internal server error"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<Void> deleteTask(@PathVariable String taskId);

    @Operation(
            summary = "Marcar tarefa como concluída",
            description = """
                    <p>Marca uma tarefa como <strong>concluída</strong> (<code>finished: true</code>).</p>
                    <p>Somente o dono da tarefa pode marcá-la como concluída.</p>
                    <p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>
                    """,
            parameters = @Parameter(
                    name = "taskId",
                    description = "UUID da tarefa a ser marcada como concluída",
                    required = true,
                    example = "b2c3d4e5-f6a7-8901-bcde-f12345678901"
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Tarefa marcada como concluída com sucesso — sem corpo de resposta"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token ausente, inválido ou expirado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Não autenticado",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Unauthorized",
                                              "status": 401,
                                              "detail": "Invalid or expired token"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não tem permissão para concluir esta tarefa",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Acesso negado",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Forbidden",
                                              "status": 403,
                                              "detail": "Access denied to this task"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Tarefa não encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Tarefa não encontrada",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Not Found",
                                              "status": 404,
                                              "detail": "Task not found"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Erro interno",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Internal Server Error",
                                              "status": 500,
                                              "detail": "Internal server error"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<Void> markAsFinished(@PathVariable String taskId);
}
