package com.wellnr.platform.core.modules.users.ports;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.modules.users.values.rbac.GrantedRoleAssignment;
import com.wellnr.platform.core.modules.users.values.rbac.RoleAssignment;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface RoleAssignmentsRepositoryPort {

    /**
     * Updates or inserts a {@link GrantedRoleAssignment}.
     *
     * @param roleAssignment The value to be stored.
     * @return Done.
     */
    CompletionStage<Done> insertOrUpdateGrantedRoleAssignment(
        GrantedRoleAssignment roleAssignment
    );

    /**
     * Find a matching {@link GrantedRoleAssignment}.
     *
     * @param assignedTo {@link RoleAssignment#getAssignedTo()}.
     * @param subject {@link RoleAssignment#getSubject()}.
     * @param role {@link RoleAssignment#getRole()}.
     * @return The stored {@link GrantedRoleAssignment} if found.
     */
    CompletionStage<Optional<GrantedRoleAssignment>> findGrantedRoleAssignment(
        GUID assignedTo, GUID subject, GUID role
    );

    CompletionStage<Optional<GrantedRoleAssignment>> findGrantedRoleAssignmentByGUID(
        GUID subject
    );

    /**
     * Find a matching {@link GrantedRoleAssignment}.
     *
     * @param roleAssignment {@link RoleAssignment}.
     * @return The stored {@link GrantedRoleAssignment} if found.
     */
    default CompletionStage<Optional<GrantedRoleAssignment>> findGrantedRoleAssignment(
        RoleAssignment roleAssignment
    ) {
        return findGrantedRoleAssignment(
            roleAssignment.getAssignedTo(), roleAssignment.getSubject(), roleAssignment.getRole().getGUID()
        );
    }

    /**
     * Removes granted role assignment.
     *
     * @param assignedTo {@link RoleAssignment#getAssignedTo()}.
     * @param subject {@link RoleAssignment#getSubject()}.
     * @param role {@link RoleAssignment#getRole()}.
     * @return Done.
     */
    CompletionStage<Done> removeGrantedRoleAssignment(
        GUID assignedTo, GUID subject, GUID role
    );

}
