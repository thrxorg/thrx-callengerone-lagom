package org.thrx.challenger.one.user.impl;

import java.util.UUID;

import org.thrx.challenger.one.user.api.User;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

public interface UserEvent extends Jsonable, AggregateEvent<UserEvent>, CompressedJsonable {

    int NUM_SHARDS = 4;
    AggregateEventShards<UserEvent> TAG = AggregateEventTag.sharded(UserEvent.class, NUM_SHARDS);

    UUID getUserId();
    User getUser();

    @Override
    default AggregateEventTagger<UserEvent> aggregateTag() {
        return TAG;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class UserCreated implements UserEvent {
        private final User user;
        private final UUID userId;
    }
	
    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class UserUpdated implements UserEvent {
    	private final User user;
    	private final UUID userId;
    }
    
    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class UserDeleted implements UserEvent {
    	private final User user;
    	private final UUID userId;
    }
    
}
