package org.thrx.challenger.one.user.impl;

import java.util.Optional;

import org.thrx.challenger.one.user.api.User;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;

public interface UserCommand extends Jsonable {
    final class CreateUser implements UserCommand, PersistentEntity.ReplyType<User> {
		private static final long serialVersionUID = 8256708300958778321L;
		private final String name;

        @JsonCreator
        public CreateUser(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CreateUser that = (CreateUser) o;

            return name.equals(that.name);

        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return "CreateUser{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    enum GetUser implements UserCommand, PersistentEntity.ReplyType<Optional<User>> {
        INSTANCE
    }
}
