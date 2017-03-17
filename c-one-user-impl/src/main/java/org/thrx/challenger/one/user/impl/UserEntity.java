package org.thrx.challenger.one.user.impl;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.thrx.challenger.one.user.api.User;
import org.thrx.challenger.one.user.impl.UserCommand.CreateUser;
import org.thrx.challenger.one.user.impl.UserCommand.GetUser;
import org.thrx.challenger.one.user.impl.UserEvent.UserCreated;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import akka.Done;

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
                ctx.reply(Optional.of(user))
        );

        b.setReadOnlyCommandHandler(CreateUser.class, (create, ctx) ->
            ctx.invalidCommand("User already exists.")
        );

        return b.build();
    }

    private Behavior notCreated() {
        BehaviorBuilder b = newBehaviorBuilder(Optional.empty()); 

        b.setReadOnlyCommandHandler(GetUser.class, (get, ctx) ->
                ctx.reply(Optional.empty())
        );

        b.setCommandHandler(CreateUser.class, (create, ctx) -> {
            User user = create.getUser();
            return ctx.thenPersist(new UserCreated(user,user.getId()), (e) -> ctx.reply(Done.getInstance()));
        });

        b.setEventHandlerChangingBehavior(UserCreated.class, user -> created(user.getUser()));

        return b.build();
    }
}
