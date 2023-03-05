package com.wellnr.platform.core.modules.users.values.users;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.modules.users.UsersModule;
import com.wellnr.platform.core.modules.users.values.rbac.RoleAssignment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Set;

@With
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RegisteredUser implements User {

    /**
     * User ID as received from external Identity Provider.
     */
    String userId;

    /**
     * The name as it should be displayed in the application.
     */
    String displayName;

    /**
     * Roles assigned to authenticated users. Usually this should be the same set for all authenticated users.
     */
    Set<RoleAssignment> roles;

    /**
     * A unique id to identify this user.
     */
    GUID guid;

    /**
     * Creates a new instance.
     *
     * @param userId See {@link RegisteredUser#userId}.
     * @param displayName See {@link RegisteredUser#displayName}.
     * @param roles See {@link RegisteredUser#roles}.
     * @return A new instance.
     */
    public static RegisteredUser apply(String userId, String displayName, Collection<RoleAssignment> roles) {
        var guid = GUID.fromString(MessageFormat.format(
            "{0}/user[id=''{1}'']",
            UsersModule.GUID_PREFIX, Operators.randomHash()
        ));

        return new RegisteredUser(userId, displayName, Set.copyOf(roles), guid);
    }

    @Override
    public GUID getGUID() {
        return guid;
    }

}
