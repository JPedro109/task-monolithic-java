package com.jpmns.task.core.application.usecase.task.implementation;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.application.usecase.task.dto.input.MarkTaskAsFinishedInputDTO;
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException;
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException;
import com.jpmns.task.core.application.usecase.task.interfaces.MarkTaskAsFinishedUseCase;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;

@Service
public class MarkTaskAsFinishedUseCaseImpl implements MarkTaskAsFinishedUseCase {

    private final TaskRepository taskRepository;

    public MarkTaskAsFinishedUseCaseImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public void execute(MarkTaskAsFinishedInputDTO input) {
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

        task.markAsFinished();
        taskRepository.save(task);
    }
}
