package org.thrx.challenger.one.user.impl;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.thrx.challenger.one.user.api.User;
import org.thrx.challenger.one.user.impl.UserCommand.CreateUser;
import org.thrx.challenger.one.user.impl.UserCommand.GetUser;
import org.thrx.challenger.one.user.impl.UserEvent.UserCreated;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

public class UserEntity extends PersistentEntity<UserCommand, UserEvent, Optional<User>> {

    @Override
    public Behavior initialBehavior(Optional<Optional<User>> snapshotState) {
        Optional<User> user = snapshotState.flatMap(Function.identity());

        if (user.isPresent()) {
            return created(user.get());
        } else {
            return notCreated();
        }
    }

    private Behavior created(User user) {
        BehaviorBuilder b = newBehaviorBuilder(Optional.of(user));

        b.setReadOnlyCommandHandler(GetUser.class, (get, ctx) ->
                ctx.reply(state())
        );

        b.setReadOnlyCommandHandler(CreateUser.class, (create, ctx) ->
            ctx.invalidCommand("User already exists.")
        );

        return b.build();
    }

    private Behavior notCreated() {
        BehaviorBuilder b = newBehaviorBuilder(Optional.empty());

        b.setReadOnlyCommandHandler(GetUser.class, (get, ctx) ->
                ctx.reply(state())
        );

        b.setCommandHandler(CreateUser.class, (create, ctx) -> {
            User user = new User(UUID.fromString(entityId()), create.getName());
            return ctx.thenPersist(new UserCreated(user), (e) -> ctx.reply(user));
        });

        b.setEventHandlerChangingBehavior(UserCreated.class, user -> created(user.getUser()));

        return b.build();
    }
}
