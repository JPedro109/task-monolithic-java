package com.jpmns.task.core.application.usecase.user.interfaces;

import com.jpmns.task.core.application.usecase.user.dto.input.RefreshUserTokenInputDTO;
import com.jpmns.task.core.application.usecase.user.dto.output.RefreshUserTokenOutputDTO;

public interface RefreshUserTokenUseCase {

    RefreshUserTokenOutputDTO execute(RefreshUserTokenInputDTO input);
}
