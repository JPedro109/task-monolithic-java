package com.jpmns.task.core.application.usecase.task.implementation;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.application.usecase.task.dto.input.DeleteTaskInputDTO;
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException;
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException;
import com.jpmns.task.core.application.usecase.task.interfaces.DeleteTaskUseCase;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;

@Service
public class DeleteTaskUseCaseImpl implements DeleteTaskUseCase {

    private final TaskRepository taskRepository;

    public DeleteTaskUseCaseImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void execute(DeleteTaskInputDTO input) {
        var taskIdValueOrError = IdValueObject.of(input.taskId());
        if (taskIdValueOrError.isFail()) {
            throw taskIdValueOrError.getError();
        }

        var taskIdValue = taskIdValueOrError.getValue();
        var task = taskRepository.findById(taskIdValue).orElseThrow(TaskNotFoundException::new);

        var userIsOwnerTask = task.getUserId().asString().equals(input.userId());
        if (!userIsOwnerTask) {
            throw new TaskAccessDeniedException();
        }

        taskRepository.deleteById(taskIdValue);
    }
}
