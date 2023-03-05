package com.wellnr.platform.core.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.modules.users.values.users.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

/**
 * Simple value class which stores base information about actions (e.g. for modified or created fields).
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMetadata {

    private static final String BY = "by";
    private static final String AT = "at";

    /**
     * The user who executed the action. The value should contain a unique, immutable user id.
     */
    @JsonProperty(BY)
    GUID by;

    /**
     * The moment when the action was executed.
     */
    @JsonProperty(AT)
    Instant at;

    /**
     * @param by The user who executed the action. The value should contain a unique, immutable user id.
     * @param at The moment when the action was executed.
     * @return A new instance
     */
    @JsonCreator
    public static EventMetadata apply(
        @JsonProperty(BY) GUID by,
        @JsonProperty(AT) Instant at) {

        return new EventMetadata(by, at);
    }

    /**
     * Creates a new instance.
     *
     * @param by The user who executed the action.
     * @return A new instance.
     */
    public static EventMetadata apply(User by) {
        return apply(by, Instant.now());
    }

    /**
     * Creates a new instance.
     *
     * @param by The unique and immutable user id for the user who executed the action.
     * @return A new instance.
     */
    public static EventMetadata apply(GUID by) {
        return apply(by, Instant.now());
    }

    /**
     * Creates a new instance.
     *
     * @param user The user who executed the action.
     * @param at   The moment when the action was executed.
     * @return A new instance.
     */
    public static EventMetadata apply(User user, Instant at) {
        return apply(user.getGUID(), at);
    }

}
