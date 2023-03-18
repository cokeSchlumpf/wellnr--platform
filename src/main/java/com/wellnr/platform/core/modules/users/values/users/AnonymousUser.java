package com.wellnr.platform.core.modules.users.values.users;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.modules.users.values.rbac.RoleAssignment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Collection;
import java.util.Set;

/**
 * An anonymous user is a user whose authentication has not been completed yet.
 * The system has no information about this user. However, it might be possible to identity a single person
 * which is currently using the application. E.g., identified by browser's session cookie.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnonymousUser implements User {

    Set<RoleAssignment> roles;

    GUID guid;

    public static AnonymousUser apply(Collection<RoleAssignment> roles) {
        return new AnonymousUser(Set.copyOf(roles), UserGUID.apply());
    }

    @Override
    public GUID getGUID() {
        return guid;
    }

}
