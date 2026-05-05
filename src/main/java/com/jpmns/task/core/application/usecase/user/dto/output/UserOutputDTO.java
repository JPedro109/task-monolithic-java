package com.jpmns.task.core.application.usecase.user.dto.output;

import java.time.Instant;

public record UserOutputDTO(String id, String username, String password, Instant createdAt, Instant updatedAt) { }
