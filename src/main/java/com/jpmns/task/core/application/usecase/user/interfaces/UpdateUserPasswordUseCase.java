package com.jpmns.task.core.application.usecase.user.interfaces;

import com.jpmns.task.core.application.usecase.user.dto.input.UpdateUserPasswordInputDTO;

public interface UpdateUserPasswordUseCase {

    void execute(UpdateUserPasswordInputDTO input);
}
