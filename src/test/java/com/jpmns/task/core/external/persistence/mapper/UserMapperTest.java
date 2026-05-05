package com.jpmns.task.core.external.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.jpmns.task.core.external.persistence.model.UserJpaModel;
import com.jpmns.task.core.fixture.UserFixture;

class UserMapperTest {

    @Test
    @DisplayName("Should map a UserEntity to a UserJpaModel correctly")
    void shouldMapEntityToModel() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var username = user.getUsername();
        var password = user.getPassword();

        var model = UserMapper.toModel(user);

        assertThat(model).isNotNull();
        assertThat(model.getId()).isEqualTo(UUID.fromString(userId.asString()));
        assertThat(model.getUsername()).isEqualTo(username.asString());
        assertThat(model.getPassword()).isEqualTo(password.asString());
        assertThat(model.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should map a UserJpaModel to a UserEntity correctly")
    void shouldMapModelToDomain() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var username = user.getUsername();
        var password = user.getPassword();

        var model = new UserJpaModel(
                UUID.fromString(userId.asString()),
                username.asString(),
                password.asString(),
                Instant.now(),
                Instant.now()
        );

        var entity = UserMapper.toDomain(model);

        assertThat(entity).isNotNull();
        assertThat(entity.getId().asString()).isEqualTo(userId.asString());
        assertThat(entity.getUsername().asString()).isEqualTo(username.asString());
        assertThat(entity.getPassword().asString()).isEqualTo(password.asString());
    }

    @Test
    @DisplayName("Should preserve username when mapping from model to domain")
    void shouldPreserveUsernameWhenMappingToDomain() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var password = user.getPassword();
        var customUsername = "custom_user";

        var model = new UserJpaModel(
                UUID.fromString(userId.asString()),
                customUsername,
                password.asString(),
                Instant.now(),
                Instant.now()
        );

        var entity = UserMapper.toDomain(model);

        assertThat(entity.getUsername().asString()).isEqualTo(customUsername);
    }

    @Test
    @DisplayName("Should preserve password when mapping from model to domain")
    void shouldPreservePasswordWhenMappingToDomain() {
        var user = UserFixture.aUser();
        var userId = user.getId();
        var username = user.getUsername();
        var encodedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

        var model = new UserJpaModel(
                UUID.fromString(userId.asString()),
                username.asString(),
                encodedPassword,
                Instant.now(),
                Instant.now()
        );

        var entity = UserMapper.toDomain(model);

        assertThat(entity.getPassword().asString()).isEqualTo(encodedPassword);
    }
}
