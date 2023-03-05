package com.wellnr.platform.core.modules.users.values.users;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.modules.users.UsersModule;
import com.wellnr.platform.core.modules.users.values.rbac.RoleAssignment;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.text.MessageFormat;
import java.util.*;

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
        var guid = GUID.fromString(MessageFormat.format(
            "{0}/anonymous-user/{1}",
            UsersModule.GUID_PREFIX, Operators.randomHash()
        ));

        return new AnonymousUser(Set.copyOf(roles), guid);
    }

    @Override
    public GUID getGUID() {
        return guid;
    }

}
