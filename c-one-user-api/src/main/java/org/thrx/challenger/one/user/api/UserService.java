package org.thrx.challenger.one.user.api;

import akka.Done;
import akka.NotUsed;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Descriptor.Call;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.lightbend.lagom.javadsl.api.transport.Method;

import org.pcollections.PSequence;

import java.util.Optional;
import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.*;
import static com.lightbend.lagom.javadsl.api.transport.Method.DELETE;
import static com.lightbend.lagom.javadsl.api.transport.Method.GET;
import static com.lightbend.lagom.javadsl.api.transport.Method.POST;
import static com.lightbend.lagom.javadsl.api.transport.Method.PUT;

public interface UserService extends Service {

    ServiceCall<User, Done> createUser();
    ServiceCall<User, Done> updateUser();

    ServiceCall<NotUsed, User> getUser(UUID userId);
    ServiceCall<NotUsed, Done> deleteUser(UUID userId);

    // Remove once we have a proper user service
    ServiceCall<NotUsed, PSequence<User>> getUsers();

    @Override
    default Descriptor descriptor() {
        return named("user").withCalls(
                restCall(POST,"/api/user", this::createUser),
                restCall(GET,"/api/user/:id", this::getUser),
                restCall(PUT,"/api/user/:id", this::updateUser),
                restCall(DELETE,"/api/user/:id", this::deleteUser),
                restCall(GET,"/api/user", this::getUsers)
        ).withAutoAcl(true)
         .withPathParamSerializer(UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString));
    }

}
