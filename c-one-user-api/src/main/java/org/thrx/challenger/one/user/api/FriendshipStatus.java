package org.thrx.challenger.one.user.api;

/**
 * The status of an item.
 */
public enum FriendshipStatus {
    /**
     * A friendship request has been sent to a user.
     */
    REQUESTED,
    /**
     * A friendship request has been accepted by user.
     */
    ACCEPTED,
    /**
     * A friendship request has been rejected by user.
     */
    REJECTED
}
