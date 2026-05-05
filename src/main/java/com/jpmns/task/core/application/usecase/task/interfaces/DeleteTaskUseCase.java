package com.jpmns.task.core.application.usecase.task.interfaces;

import com.jpmns.task.core.application.usecase.task.dto.input.DeleteTaskInputDTO;

public interface DeleteTaskUseCase {

    void execute(DeleteTaskInputDTO input);
}
