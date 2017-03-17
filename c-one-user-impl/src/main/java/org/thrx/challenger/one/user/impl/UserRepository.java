package org.thrx.challenger.one.user.impl;

import static org.thrx.challenger.one.user.impl.CompletionStageUtils.doAll;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.thrx.challenger.one.user.api.FriendshipStatus;
import org.thrx.challenger.one.user.api.User;
import org.thrx.challenger.one.user.impl.UserEvent.UserCreated;
import org.thrx.challenger.one.user.impl.UserEvent.UserDeleted;
import org.thrx.challenger.one.user.impl.UserEvent.UserUpdated;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.extras.codecs.enums.EnumNameCodec;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import akka.Done;
import de.thrx.cone.common.PaginatedSequence;

@Singleton
public class UserRepository { 

    private final CassandraSession session;

    @Inject
    public UserRepository(CassandraSession session, ReadSide readSide) {
        this.session = session;
        readSide.register(UserEventProcessor.class);
    }


    CompletionStage<PaginatedSequence<User>> getUsers(int page, int pageSize){
        return countUsers()
                .thenCompose(
                        count -> {
                            int offset = page * pageSize;
                            int limit = (page + 1) * pageSize;
                            CompletionStage<PSequence<User>> users = offset > count ?
                                    CompletableFuture.completedFuture(TreePVector.empty()) :
                                    	selectUsers(offset, limit);
                            return users.thenApply(seq -> new PaginatedSequence<>(seq, page, pageSize, count));
                        }
                );
    }
    
    private CompletionStage<Integer> countUsers() {
        return session
                .selectOne(
                        "SELECT COUNT(*) FROM User "
                        		+ "ORDER BY id ASC"
                        // ORDER BY status is required due to https://issues.apache.org/jira/browse/CASSANDRA-10271
                )
                .thenApply(row -> (int) row.get().getLong("count"));
    }

    private CompletionStage<PSequence<User>> selectUsers(
            long offset, int limit) {
        return session
                .selectAll(
                        "SELECT * FROM User " 
                        		+ " ORDER BY id ASC"
                                // ORDER BY status is required due to https://issues.apache.org/jira/browse/CASSANDRA-10271
                                + " LIMIT ?" ,
                        limit
                )
                .thenApply(List::stream)
                .thenApply(rows -> rows.skip(offset))
                .thenApply(rows -> rows.map(UserRepository::convertUser))
                .thenApply(users -> users.collect(Collectors.toList()))
                .thenApply(TreePVector::from);
    }
  
    private static User convertUser(Row item) {
        return new User(
                item.getUUID("id"),
                item.getString("username"),
                item.getString("nickName"),
                item.getString("givenName"),
                item.getString("familyname"),
                item.getString("postCode"),
                item.getString("eMail"),
                item.getString("mobilPhone"),
                item.getString("street"),
                item.getString("city")
        );
    }
    
    private static class UserEventProcessor extends ReadSideProcessor<UserEvent> {

        private final CassandraSession session;
        private final CassandraReadSide readSide;
		private PreparedStatement writeUser;
	    private PreparedStatement deleteUser;


        @Inject
        public UserEventProcessor(CassandraSession session, CassandraReadSide readSide) {
            this.session = session;
            this.readSide = readSide;
        }

        @Override
        public ReadSideHandler<UserEvent> buildHandler() {
            return readSide.<UserEvent>builder("userEventReadSideId")
                    .setGlobalPrepare(this::createTables)
                    .setPrepare(tag -> prepareStatements())
//                    .setEventHandler(UserCreated.class, e -> insertUser(e.getUser()))
//                    .setEventHandler(UserUpdated.class, e -> updateUser(e))
                    .setEventHandler(UserCreated.class, this::processPostAdded)
                    .setEventHandler(UserUpdated.class, this::processPostUpdated)
                    .setEventHandler(UserDeleted.class, this::processPostDeleted)
                    .build();
        }

		@Override
        public PSequence<AggregateEventTag<UserEvent>> aggregateTags() {
            return UserEvent.TAG.allTags();
        }

		
	    // Execute only once while application is start
        private CompletionStage<Done> createTables() {
            return doAll(
        	        session.executeCreateTable(
        	                "CREATE TABLE IF NOT EXISTS User ("
        	                        + "  id UUID"
        	                        + ", username TEXT"
        	                        + ", nickName TEXT"
        	                        + ", givenName TEXT"
        	                        + ", familyname TEXT"
        	                        + ", postCode TEXT"
        	                        + ", eMail TEXT"
        	                        + ", mobilPhone TEXT"
        	                        + ", street TEXT"
        	                        + ", city TEXT"
        	                        + ", PRIMARY KEY(id)"
        	                        + ")"
        	        )
            );
        }

        private void registerCodec(Session session, TypeCodec<?> codec) {
            session.getCluster().getConfiguration().getCodecRegistry().register(codec);
        }

        private CompletionStage<Done> prepareStatements() {
            return doAll( 
                    session.underlying()
                            .thenAccept(s -> registerCodec(s, new EnumNameCodec<>(FriendshipStatus.class)))
                            .thenApply(x -> Done.getInstance()),
                            prepareWriteUser(),
                            prepareDeleteUser()
            );
        }

        /*
         * START: Prepare statement for insert User values into User table.
         * This is just creation of prepared statement, we will map this statement with our event
         */

         /**
          *
          * @return
          */
         private CompletionStage<Done> prepareWriteUser() {
             return session.prepare(
                     "INSERT INTO User ("
 	                        + "  id "
 	                        + ", username"
 	                        + ", nickName"
 	                        + ", givenName"
 	                        + ", familyname"
 	                        + ", postCode"
 	                        + ", eMail"
 	                        + ", mobilPhone"
 	                        + ", street"
 	                        + ", city"
                     + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
             ).thenApply(ps -> {
                 setWriteUser(ps);
                 return Done.getInstance();
             });
         }

         /**
          *
          * @param statement
          */
         private void setWriteUser(PreparedStatement statement) {
             this.writeUser = statement;
         }

         // Bind prepare statement while UserCreate event is executed

         /**
          *
          * @param event
          * @return
          */
         private CompletionStage<List<BoundStatement>> processPostAdded(UserCreated event) {
             BoundStatement bindWriteUser = writeUser.bind();
             bindWriteUser.setUUID("id", event.getUser().getId());
	     	 bindWriteUser.setString("username", event.getUser().getUsername());
	     	 bindWriteUser.setString("nickName", event.getUser().getNickName());
	    	 bindWriteUser.setString("givenName", event.getUser().getGivenName());
	    	 bindWriteUser.setString("familyname", event.getUser().getFamilyname());
	    	 bindWriteUser.setString("postCode", event.getUser().getPostCode());
	    	 bindWriteUser.setString("eMail", event.getUser().getEMail());
	    	 bindWriteUser.setString("mobilPhone", event.getUser().getMobilPhone());
	    	 bindWriteUser.setString("street", event.getUser().getStreet());
	    	 bindWriteUser.setString("city", event.getUser().getCity());
             return CassandraReadSide.completedStatements(Arrays.asList(bindWriteUser));
         }
         /* ******************* END ****************************/

         /* START: Prepare statement for update the data in User table.
         * This is just creation of prepared statement, we will map this statement with our event
         */

         /**
          *
          * @param event
          * @return
          */
         private CompletionStage<List<BoundStatement>> processPostUpdated(UserUpdated event) {
             BoundStatement bindWriteUser = writeUser.bind();
             bindWriteUser.setUUID("id", event.getUser().getId());
	     	 bindWriteUser.setString("username", event.getUser().getUsername());
	     	 bindWriteUser.setString("nickName", event.getUser().getNickName());
	    	 bindWriteUser.setString("givenName", event.getUser().getGivenName());
	    	 bindWriteUser.setString("familyname", event.getUser().getFamilyname());
	    	 bindWriteUser.setString("postCode", event.getUser().getPostCode());
	    	 bindWriteUser.setString("eMail", event.getUser().getEMail());
	    	 bindWriteUser.setString("mobilPhone", event.getUser().getMobilPhone());
	    	 bindWriteUser.setString("street", event.getUser().getStreet());
	    	 bindWriteUser.setString("city", event.getUser().getCity());
             return CassandraReadSide.completedStatements(Arrays.asList(bindWriteUser));
         }
         /* ******************* END ****************************/

         /* START: Prepare statement for delete the the User from table.
         * This is just creation of prepared statement, we will map this statement with our event
         */

         /**
          *
          * @return
          */
         private CompletionStage<Done> prepareDeleteUser() {
             return session.prepare(
                     "DELETE FROM User WHERE id=?"
             ).thenApply(ps -> {
                 setDeleteUser(ps);
                 return Done.getInstance();
             });
         }

         /**
          *
          * @param deleteUser
          */
         private void setDeleteUser(PreparedStatement deleteUser) {
             this.deleteUser = deleteUser;
         }

         /**
          *
          * @param event
          * @return
          */
         private CompletionStage<List<BoundStatement>> processPostDeleted(UserDeleted event) {
             BoundStatement bindWriteUser = deleteUser.bind();
             bindWriteUser.setUUID("id", event.getUser().getId());
             return CassandraReadSide.completedStatements(Arrays.asList(bindWriteUser));
         }
         /* ******************* END ****************************/
    }


}
