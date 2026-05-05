package com.jpmns.task.core.external.persistence.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jpmns.task.core.external.persistence.model.UserJpaModel;

public interface UserJpaDao extends JpaRepository<UserJpaModel, UUID> {

    Optional<UserJpaModel> findByUsername(String username);

    boolean existsByUsername(String username);
}
