package com.jpmns.task.core.application.usecase.task.implementation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.application.usecase.task.dto.input.ListTasksInputDTO;
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO;
import com.jpmns.task.core.application.usecase.task.interfaces.ListTasksUseCase;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.core.domain.task.TaskEntity;

@Service
public class ListTasksUseCaseImpl implements ListTasksUseCase {

    private final TaskRepository taskRepository;

    public ListTasksUseCaseImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<TaskOutputDTO> execute(ListTasksInputDTO input) {
        var userIdValueOrError = IdValueObject.of(input.userId());
        if (userIdValueOrError.isFail()) {
            throw userIdValueOrError.getError();
        }

        var userIdValue = userIdValueOrError.getValue();
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
