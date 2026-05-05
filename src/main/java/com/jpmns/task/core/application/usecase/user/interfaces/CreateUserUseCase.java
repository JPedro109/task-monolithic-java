package com.jpmns.task.core.application.usecase.user.interfaces;

import com.jpmns.task.core.application.usecase.user.dto.input.CreateUserInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.CreateUserOutputDTO;

public interface CreateUserUseCase {

    CreateUserOutputDTO execute(CreateUserInputDTO input);
}
