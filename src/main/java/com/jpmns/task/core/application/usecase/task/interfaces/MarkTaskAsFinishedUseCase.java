package com.jpmns.task.core.application.usecase.task.interfaces;

import com.jpmns.task.core.application.usecase.task.dto.input.MarkTaskAsFinishedInputDTO;

public interface MarkTaskAsFinishedUseCase {

    void execute(MarkTaskAsFinishedInputDTO input);
}
