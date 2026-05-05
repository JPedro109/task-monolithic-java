package com.jpmns.task.core.application.usecase.task.interfaces;

import java.util.List;

import com.jpmns.task.core.application.usecase.task.dto.input.ListTasksInputDTO;
import com.jpmns.task.core.application.usecase.task.dto.output.TaskOutputDTO;

public interface ListTasksUseCase {

    List<TaskOutputDTO> execute(ListTasksInputDTO input);
}
