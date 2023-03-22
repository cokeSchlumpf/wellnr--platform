package com.wellnr.platform.core.modules.users.values.rbac;

import com.wellnr.platform.common.guid.GUID;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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
    Set<String> permissions;

    /**
     * Creates a new instance. Alternative constructor for easy instantiation via code.
     *
     * @param guid The GUID as string.
     * @param permissions The list of permission names.
     * @return A new instance.
     */
    public static Role apply(String guid, String ...permissions) {
        return apply(
            GUID.fromString(guid),
            Arrays.stream(permissions).collect(Collectors.toSet())
        );
    }

    public GUID getGUID() {
        return guid;
    }
}
