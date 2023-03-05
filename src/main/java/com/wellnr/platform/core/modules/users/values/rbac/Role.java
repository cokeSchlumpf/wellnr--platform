package com.wellnr.platform.core.modules.users.values.rbac;

import com.wellnr.platform.common.guid.GUID;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Set;

/**
 * A role bundles permissions. A role can be assigned to users or a group of users. See also {@link RoleAssignment}.
 */
@Value
@AllArgsConstructor(staticName = "apply")
public class Role {

    /**
     * The unique id of the role.
     */
    GUID guid;

    /**
     * The list of permissions allowed by this role.
     */
    Set<Permission> permissions;

    public GUID getGUID() {
        return guid;
    }
}
