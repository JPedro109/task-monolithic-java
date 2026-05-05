package com.jpmns.task.core.application.usecase.user.interfaces;

import com.jpmns.task.core.application.usecase.user.dto.input.DeleteUserInputDTO;

public interface DeleteUserUseCase {

    void execute(DeleteUserInputDTO input);
}
