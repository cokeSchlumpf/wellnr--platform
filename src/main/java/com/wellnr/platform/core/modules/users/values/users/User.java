package com.wellnr.platform.core.modules.users.values.users;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.modules.users.values.rbac.Permission;
import com.wellnr.platform.core.modules.users.values.rbac.RoleAssignment;

import java.util.Set;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type")
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = AnonymousUser.class, name = "anonymous"),
        @JsonSubTypes.Type(value = AuthenticatedUser.class, name = "authenticated"),
        @JsonSubTypes.Type(value = RegisteredUser.class, name = "registered"),
    })
public sealed interface User permits AnonymousUser, AuthenticatedUser, RegisteredUser {

    @JsonProperty("guid")
    GUID getGUID();

    /**
     * Returns the list of RBAC roles assigned to the user.
     *
     * @return The list of roles.
     */
    @JsonProperty("roles")
    Set<RoleAssignment> getRoles();

    /**
     * Check whether the user has a specified permission.
     *
     * @param subject The target subject (resource) on which the permission needs to be granted.
     * @param permission The permission which is required.
     * @return True or false.
     */
    default boolean hasPermission(GUID subject, Permission permission) {
        return this
            .getRoles()
            .stream()
            .filter(role -> role.getSubject().equals(subject))
            .flatMap(role -> role.getRole().getPermissions().stream())
            .anyMatch(permission::equals);
    }

}

