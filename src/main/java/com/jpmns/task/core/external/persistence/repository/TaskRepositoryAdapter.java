package com.jpmns.task.core.external.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.jpmns.task.core.application.port.persistence.repository.TaskRepository;
import com.jpmns.task.core.domain.common.valueobject.IdValueObject;
import com.jpmns.task.core.domain.task.TaskEntity;
import com.jpmns.task.core.external.persistence.dao.TaskJpaDao;
import com.jpmns.task.core.external.persistence.mapper.TaskMapper;

@Repository
public class TaskRepositoryAdapter implements TaskRepository {

    private final TaskJpaDao jpaRepository;

    public TaskRepositoryAdapter(TaskJpaDao jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TaskEntity save(TaskEntity task) {
        var model = TaskMapper.toModel(task);

        var saved = jpaRepository.save(model);

        return TaskMapper.toDomain(saved);
    }

    @Override
    public Optional<TaskEntity> findById(IdValueObject id) {
        var parsedId = UUID.fromString(id.asString());

        return jpaRepository.findById(parsedId).map(TaskMapper::toDomain);
    }

    @Override
    public List<TaskEntity> findAllByUserId(IdValueObject userId) {
        return jpaRepository.findAllByUserId(UUID.fromString(userId.asString()))
                .stream()
                .map(TaskMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(IdValueObject id) {
        var parsedId = UUID.fromString(id.asString());

        jpaRepository.deleteById(parsedId);
    }
}
