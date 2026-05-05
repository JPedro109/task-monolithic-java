package com.jpmns.task.core.application.usecase.user.interfaces;

import com.jpmns.task.core.application.usecase.user.dto.input.UserLoginInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.UserLoginOutputDTO;

public interface UserLoginUseCase {

    UserLoginOutputDTO execute(UserLoginInputDTO input);
}
