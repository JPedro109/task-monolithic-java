package com.jpmns.task.core.domain.common.abstracts;

import java.time.Instant;
import java.util.List;

import com.jpmns.task.core.domain.common.exception.DomainException;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.shared.type.Result;

public abstract class Entity {

    private final IdValueObject id;
    private final Instant createdAt;

    public Entity(String id, Instant createdAt) {
        var idResult = IdValueObject.of(id);
        validateOrThrow(List.of(idResult));

        this.id = idResult.getValue();

        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public IdValueObject getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    protected void validateOrThrow(List<Result<?, DomainException>> results) {
        var errors = results.stream()
                .filter(Result::isFail)
                .map(Result::getError)
                .toList();

        if (!errors.isEmpty()) {
            throw DomainException.with(errors);
        }
    }
}
