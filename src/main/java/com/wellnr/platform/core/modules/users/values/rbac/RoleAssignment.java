package com.wellnr.platform.core.modules.users.values.rbac;

import com.wellnr.platform.common.guid.GUID;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * A role assignment specifies that a user or group of uses is assigned to a role.
 */
@Value
@AllArgsConstructor(staticName = "apply")
public class RoleAssignment {

    /**
     * The GUID for the resource (must be user or group) to which the role is assigned.
     */
    GUID assignedTo;

    /**
     * A GUID for the resource for which the role is assigned.
     */
    GUID subject;

    /**
     * The role to which the user is assigned
     */
    Role role;

}
