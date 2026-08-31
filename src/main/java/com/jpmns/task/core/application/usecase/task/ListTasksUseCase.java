package com.jpmns.task.core.application.usecase.task;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.application.usecase.task.dto.input.ListTasksInputDTO;
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.core.domain.task.TaskEntity;

@Service
public class ListTasksUseCase {

    private final TaskRepository taskRepository;

    public ListTasksUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskOutputDTO> execute(ListTasksInputDTO input) {
        var userIdValue = IdValueObject.of(input.userId()).getValueOrThrow();

        return taskRepository.findAllByUserId(userIdValue)
                .stream()
                .map(this::toOutput)
                .toList();
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
