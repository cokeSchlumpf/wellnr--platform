package com.wellnr.platform.core.modules.users.values.users;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.modules.users.UsersModule;
import com.wellnr.platform.core.modules.users.values.rbac.RoleAssignment;
import lombok.*;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * For an authenticates user, the system already knows its unique external user identifier - Provided by an
 * identity provider, but the user is not registered in the application's user database and thus
 * is not a {@link RegisteredUser} yet.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthenticatedUser implements User {

    /**
     * User ID as received from external Identity Provider.
     */
    String userId;

    /**
     * Additional properties of the user which might be known already.
     */
    AdditionalProperties additionalProperties;

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
     * @param userId See {@link AuthenticatedUser#userId}.
     * @param roles See {@link AuthenticatedUser#roles}.
     * @param properties See {@link AuthenticatedUser#additionalProperties}.
     * @return A new instance.
     */
    public static AuthenticatedUser apply(String userId, Collection<RoleAssignment> roles, AdditionalProperties properties) {
        var guid = GUID.fromString(MessageFormat.format(
            "{0}/authenticated-user/{1}",
            UsersModule.GUID_PREFIX, Operators.randomHash()
        ));

        return new AuthenticatedUser(userId, properties, Set.copyOf(roles), guid);
    }

    /**
     * Creates a new instance.
     *
     * @param userId See {@link AuthenticatedUser#userId}.
     * @param roles See {@link AuthenticatedUser#roles}.
     * @return A new instance.
     */
    public static AuthenticatedUser apply(String userId, Collection<RoleAssignment> roles) {
        return apply(userId, roles, AdditionalProperties.apply());
    }

    @Override
    public GUID getGUID() {
        return guid;
    }

    /**
     * A set of user properties which might have been already collected. E.g, by identity provider.
     * All these properties are optional.
     */
    @With
    @NoArgsConstructor(staticName = "apply")
    @AllArgsConstructor(staticName = "apply")
    public static class AdditionalProperties {

        String email;

        String firstName;

        String lastName;

        String fullName;

        public Optional<String> getEmail() {
            return Optional.ofNullable(email);
        }

        public Optional<String> firstName() {
            return Optional.ofNullable(firstName);
        }

        public Optional<String> getLastName() {
            return Optional.ofNullable(lastName);
        }

        public Optional<String> getFullName() {
            return Optional.ofNullable(fullName);
        }
    }

}
