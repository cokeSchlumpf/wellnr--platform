package com.wellnr.platform.core.modules.users.values.users;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.guid.HasGUID;
import com.wellnr.platform.core.modules.users.values.rbac.RoleAssignment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.Collection;
import java.util.Set;

@With
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RegisteredUser implements User, HasGUID {

    /**
     * User ID as received from external Identity Provider.
     */
    String externalUserId;

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
     * @param externalUserId See {@link RegisteredUser#externalUserId}.
     * @param displayName See {@link RegisteredUser#displayName}.
     * @param roles See {@link RegisteredUser#roles}.
     * @return A new instance.
     */
    public static RegisteredUser apply(String externalUserId, String displayName, Collection<RoleAssignment> roles) {
        return new RegisteredUser(externalUserId, displayName, Set.copyOf(roles), UserGUID.apply(externalUserId));
    }

    @Override
    public GUID getGUID() {
        return guid;
    }

}
