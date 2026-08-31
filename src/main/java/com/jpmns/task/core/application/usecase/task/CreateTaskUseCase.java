package com.jpmns.task.core.application.usecase.task;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.application.usecase.task.dto.input.CreateTaskInputDTO;
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO;
import com.jpmns.task.core.domain.task.TaskEntity;

@Service
public class CreateTaskUseCase {

    private final TaskRepository taskRepository;

    public CreateTaskUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskOutputDTO execute(CreateTaskInputDTO input) {
        var task = new TaskEntity(UUID.randomUUID().toString(), input.userId(), input.taskName(), false);
        var saved = taskRepository.save(task);

        return toOutput(saved);
    }

    private TaskOutputDTO toOutput(TaskEntity task) {
        return new TaskOutputDTO(
                task.getId().asString(),
                task.getUserId().asString(),
                task.getTaskName().asString(),
                task.getFinished(),
                task.getCreatedAt()
        );
    }
}
