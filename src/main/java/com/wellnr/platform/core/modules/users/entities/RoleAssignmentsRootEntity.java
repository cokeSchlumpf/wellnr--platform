package com.wellnr.platform.core.modules.users.entities;

import com.wellnr.platform.common.async.Writes;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.context.RootEntity;
import com.wellnr.platform.core.modules.users.values.users.User;
import com.wellnr.platform.core.modules.users.values.rbac.RoleAssignment;

import java.util.concurrent.CompletionStage;

public interface RoleAssignmentsRootEntity extends RootEntity {

    /**
     * Register a new role assignment.
     *
     * @param executor The user registering the role assignment.
     * @param assignment The new role assignment.
     * @return Done.
     */
    @Writes
    CompletionStage<Done> createRoleAssignment(User executor, RoleAssignment assignment);

}
