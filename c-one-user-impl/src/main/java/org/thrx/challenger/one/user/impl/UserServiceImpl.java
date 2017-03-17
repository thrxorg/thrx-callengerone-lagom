package org.thrx.challenger.one.user.impl;

import static de.thrx.cone.security.ServerSecurity.authenticated;

import java.util.UUID;

import javax.inject.Inject;

import org.pcollections.PSequence;
import org.thrx.challenger.one.user.api.User;
import org.thrx.challenger.one.user.api.UserService;

import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import akka.Done;
import akka.NotUsed;

public class UserServiceImpl implements UserService {

	private final PersistentEntityRegistry registry;
//	private final CurrentPersistenceIdsQuery currentIdsQuery;
//	private final Materializer mat;
	private final UserRepository userRepository;

	@Inject
	public UserServiceImpl(PersistentEntityRegistry registry, UserRepository userRepository) {
//		public UserServiceImpl(PersistentEntityRegistry registry, ActorSystem system, Materializer mat) {
		this.registry = registry;
		this.userRepository = userRepository;
//		this.mat = mat;
//		this.currentIdsQuery = PersistenceQuery.get(system).getReadJournalFor(CassandraReadJournal.class, CassandraReadJournal.Identifier());
		registry.register(UserEntity.class);
	}

	@Override
	public ServiceCall<User, Done> createUser() {
		return authenticated(userId -> reqUser -> {
			UUID uuid = UUID.randomUUID();
			return entityRef(uuid).ask(UserCommand.CreateUser.builder().user(reqUser).build());
		});
	}

	@Override
	public ServiceCall<NotUsed, User> getUser(UUID userId) {
		return req -> {
			return entityRef(userId).ask(UserCommand.GetUser.builder().build())
					.thenApply(maybeUser -> maybeUser.orElseGet(() -> {
						throw new NotFound("User " + userId + " not found");
					}));
		};
	}

	 @Override
	 public ServiceCall<NotUsed, PSequence<User>> getUsers() {
		 return req -> {
			 userRepository.hashCode();
			 return null;
		 };
		 
	 }

//	    @Override
//	    public ServiceCall<NotUsed, PaginatedSequence<ItemSummary>> getItemsForUser(
//	            UUID id, ItemStatus status, Optional<Integer> pageNo, Optional<Integer> pageSize) {
//	        return req -> items.getItemsForUser(id, status, pageNo.orElse(0), pageSize.orElse(DEFAULT_PAGE_SIZE));
//	    }
	 
	 
	@Override
	public ServiceCall<User, Done> updateUser() {
		return authenticated(userId -> reqUser -> {
			return entityRef(reqUser.getId()).ask(UserCommand.UpdateUser.builder().user(reqUser).build());
		});
	}

	@Override
	public ServiceCall<NotUsed, Done> deleteUser(UUID userId) {
		return req -> {
			// return
			// entityRef(userId).ask(UserCommand.DeleteUser.builder().build());
			// TODO alternativ test .... funktioniert das oben auch ???:
			User user = User.builder().id(userId).build();
			return entityRef(userId).ask(UserCommand.DeleteUser.builder().user(user).build());
		};
	}

	private PersistentEntityRef<UserCommand> entityRef(UUID id) {
		return entityRef(id.toString());
	}

	private PersistentEntityRef<UserCommand> entityRef(String id) {
		return registry.refFor(UserEntity.class, id);
	}

}
