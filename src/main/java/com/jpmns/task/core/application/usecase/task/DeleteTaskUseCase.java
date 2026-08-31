package com.jpmns.task.core.application.usecase.task;

import org.springframework.stereotype.Service;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.application.usecase.task.dto.input.DeleteTaskInputDTO;
import com.jpmns.task.core.application.usecase.task.exception.TaskAccessDeniedException;
import com.jpmns.task.core.application.usecase.task.exception.TaskNotFoundException;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;

@Service
public class DeleteTaskUseCase {

    private final TaskRepository taskRepository;

    public DeleteTaskUseCase(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void execute(DeleteTaskInputDTO input) {
        var taskIdValue = IdValueObject.of(input.taskId()).getValueOrThrow();

        var task = taskRepository.findById(taskIdValue).orElseThrow(TaskNotFoundException::new);

        var userIsOwnerTask = task.getUserId().asString().equals(input.userId());
        if (!userIsOwnerTask) {
            throw new TaskAccessDeniedException();
        }

        taskRepository.deleteById(taskIdValue);
    }
}
