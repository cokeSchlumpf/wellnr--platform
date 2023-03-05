package com.wellnr.platform.core.modules.users.values.rbac;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.values.EventMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GrantedRoleAssignmentMemento {

    private static final String GRANTED = "granted";
    private static final String ASSIGNED_TO = "assigned-to";
    private static final String SUBJECT = "subject";
    private static final String ROLE = "role";

    /**
     * Metadata about the grant.
     */
    @JsonProperty(GRANTED)
    EventMetadata granted;

    /**
     * The GUID for the resource (must be user or group) to which the role is assigned.
     */
    @JsonProperty(ASSIGNED_TO)
    GUID assignedTo;

    /**
     * A GUID for the resource for which the role is assigned.
     */
    @JsonProperty(SUBJECT)
    GUID subject;

    /**
     * The role to which the user is assigned
     */
    @JsonProperty(ROLE)
    GUID role;

    /**
     * Creates a new instance.
     *
     * @param granted    See {@link GrantedRoleAssignmentMemento#granted}.
     * @param assignedTo See {@link GrantedRoleAssignmentMemento#assignedTo}.
     * @param subject    See {@link GrantedRoleAssignmentMemento#subject}.
     * @param role       See {@link GrantedRoleAssignmentMemento#role}.
     * @return A new instance.
     */
    @JsonCreator
    public static GrantedRoleAssignmentMemento apply(
        @JsonProperty(GRANTED) EventMetadata granted,
        @JsonProperty(ASSIGNED_TO) GUID assignedTo,
        @JsonProperty(SUBJECT) GUID subject,
        @JsonProperty(ROLE) GUID role
    ) {
        return new GrantedRoleAssignmentMemento(granted, assignedTo, subject, role);
    }

}
