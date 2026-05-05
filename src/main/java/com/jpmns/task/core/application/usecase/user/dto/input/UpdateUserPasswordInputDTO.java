package com.jpmns.task.core.application.usecase.user.dto.input;

public record UpdateUserPasswordInputDTO(String userId, String currentPassword, String newPassword) { }
