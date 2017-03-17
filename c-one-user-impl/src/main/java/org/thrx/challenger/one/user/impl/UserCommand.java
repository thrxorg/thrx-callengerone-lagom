package org.thrx.challenger.one.user.impl;

import java.util.Optional;
import org.thrx.challenger.one.user.api.User;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;

import akka.Done;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

public interface UserCommand extends Jsonable {

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class CreateUser implements UserCommand, PersistentEntity.ReplyType<Done> {
        User user;
    }
    
    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class GetUser implements UserCommand, PersistentEntity.ReplyType<Optional<User>> {
    	Optional<User> user;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class UpdateUser implements UserCommand, PersistentEntity.ReplyType<Done> {
        User user;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class DeleteUser implements UserCommand, PersistentEntity.ReplyType<Done> {
        User user;
    }

}
