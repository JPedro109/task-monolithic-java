# Task Service

Uma API RESTful para gerenciamento pessoal de tarefas, construída com Java 21 e Spring Boot 4, seguindo os princípios da Clean Architecture.

## O que faz

- Usuários se registram e se autenticam via JWT (tokens de acesso + refresh)
- Cada usuário autenticado pode criar, listar, atualizar, excluir e marcar suas próprias tarefas como concluídas
- As tarefas são estritamente isoladas por usuário — cada usuário acessa apenas seus próprios dados

## Superfície da API

| Domínio | Caminho base |
|---------|-------------|
| Auth    | `/api/v1/auth` |
| Usuários | `/api/v1/users` |
| Tarefas | `/api/v1/tasks` |

O Swagger UI está disponível em `http://localhost:8080/swagger-ui.html` quando a aplicação estiver rodando.

## Observabilidade

O serviço emite métricas e traces via OpenTelemetry. Uma stack completa do Docker Compose sobe o PostgreSQL, Prometheus, Grafana (com dashboard pré-provisionado) e um OTEL Collector junto com a aplicação.
