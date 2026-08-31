package com.jpmns.task.core.application.usecase.task;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.application.usecase.task.dto.input.MarkTaskAsFinishedInputDTO;
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException;
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;

@Service
public class MarkTaskAsFinishedUseCase {

    private final TaskRepository taskRepository;

    public MarkTaskAsFinishedUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void execute(MarkTaskAsFinishedInputDTO input) {
        var taskIdValue = IdValueObject.of(input.taskId()).getValueOrThrow();

        var task = taskRepository.findById(taskIdValue).orElseThrow(TaskNotFoundException::new);

        var userIsOwnerTask = task.getUserId().asString().equals(input.userId());
        if (!userIsOwnerTask) {
            throw new TaskAccessDeniedException();
        }

        task.markAsFinished();
        taskRepository.save(task);
    }
}
