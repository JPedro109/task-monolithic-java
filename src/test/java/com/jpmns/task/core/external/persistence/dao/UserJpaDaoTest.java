package com.jpmns.task.core.external.persistence.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.jpmns.task.core.domain.user.UserEntity;
import com.jpmns.task.core.external.persistence.model.UserJpaModel;
import com.jpmns.task.core.fixture.UserFixture;

@DataJpaTest
@DisplayName("UserJpaDao Tests")
class UserJpaDaoTest {

    @Autowired
    private UserJpaDao userJpaDao;

    @BeforeEach
    void setUp() {
        userJpaDao.deleteAll();
    }

    private UserJpaModel buildUser(UserEntity user) {
        var id = user.getId();
        var username = user.getUsername();
        var password = user.getPassword();
        var createdAt = user.getCreatedAt();

        return new UserJpaModel(
                UUID.fromString(id.asString()),
                username.asString(),
                password.asString(),
                createdAt,
                null
        );
    }

    @Test
    @DisplayName("Should save a user and return it with a populated id")
    void shouldSaveUser() {
        var user = UserFixture.aUser();
        var model = buildUser(user);

        var saved = userJpaDao.save(model);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(model.getId());
        assertThat(saved.getUsername()).isEqualTo(model.getUsername());
        assertThat(saved.getPassword()).isEqualTo(user.getPassword().asString());
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should find a user by id after saving")
    void shouldFindUserById() {
        var user = UserFixture.aUser();
        var model = buildUser(user);
        userJpaDao.save(model);

        var found = userJpaDao.findById(model.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(model.getId());
        assertThat(found.get().getUsername()).isEqualTo(model.getUsername());
    }

    @Test
    @DisplayName("Should return empty Optional when user id does not exist")
    void shouldReturnEmptyWhenUserNotFoundById() {
        var found = userJpaDao.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find a user by username after saving")
    void shouldFindUserByUsername() {
        var user = UserFixture.aUser();
        var model = buildUser(user);
        userJpaDao.save(model);

        var found = userJpaDao.findByUsername(model.getUsername());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(model.getUsername());
    }

    @Test
    @DisplayName("Should return empty Optional when username does not exist")
    void shouldReturnEmptyWhenUsernameNotFound() {
        var found = userJpaDao.findByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return true when username already exists")
    void shouldReturnTrueWhenUsernameExists() {
        var user = UserFixture.aUser();
        var model = buildUser(user);
        userJpaDao.save(model);

        var exists = userJpaDao.existsByUsername(model.getUsername());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when username does not exist")
    void shouldReturnFalseWhenUsernameDoesNotExist() {
        var exists = userJpaDao.existsByUsername("ghost");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should delete a user by id")
    void shouldDeleteUserById() {
        var user = UserFixture.aUser();
        var model = buildUser(user);
        userJpaDao.save(model);
        userJpaDao.deleteById(model.getId());

        var found = userJpaDao.findById(model.getId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should update username when saving an existing user")
    void shouldUpdateUsernameWhenSavingExistingUser() {
        var user = UserFixture.aUser();
        var model = buildUser(user);
        userJpaDao.save(model);
        model.setUsername("updated_username");

        userJpaDao.save(model);

        var found = userJpaDao.findById(model.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(model.getUsername());
    }

    @Test
    @DisplayName("Should throw when saving a user with a duplicate username")
    void shouldThrowWhenSavingDuplicateUsername() {
        var user = UserFixture.aUser();
        var model = buildUser(user);
        userJpaDao.save(model);
        userJpaDao.flush();
        var duplicate = new UserJpaModel(
                UUID.randomUUID(),
                model.getUsername(),
                model.getPassword(),
                Instant.now(),
                null
        );

        assertThatThrownBy(() -> {
            userJpaDao.save(duplicate);
            userJpaDao.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should find all saved users")
    void shouldFindAllUsers() {
        var userOne = UserFixture.aUser();
        var model = buildUser(userOne);
        userJpaDao.save(model);

        var all = userJpaDao.findAll();

        assertThat(all).hasSize(1);
    }
}
