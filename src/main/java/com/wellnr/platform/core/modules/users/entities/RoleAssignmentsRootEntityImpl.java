package com.wellnr.platform.core.modules.users.entities;

import com.wellnr.platform.common.async.AsyncMethod;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.context.RootEntity;
import com.wellnr.platform.core.modules.users.UsersModule;
import com.wellnr.platform.core.modules.users.ports.RoleAssignmentsRepositoryPort;
import com.wellnr.platform.core.modules.users.values.rbac.GrantedRoleAssignment;
import com.wellnr.platform.core.modules.users.values.rbac.RoleAssignment;
import com.wellnr.platform.core.modules.users.values.users.User;
import com.wellnr.platform.core.values.EventMetadata;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.util.concurrent.CompletionStage;

import static com.wellnr.platform.common.Operators.completed;

@AllArgsConstructor(staticName = "apply")
public class RoleAssignmentsRootEntityImpl implements RootEntity {

    private final RoleAssignmentsRepositoryPort repository;

    @Override
    @AsyncMethod(pure = true)
    public GUID getGUID() {
        return GUID.apply(MessageFormat.format(
            "{0}/entities/role-assignments", UsersModule.GUID_PREFIX
        ));
    }

    @AsyncMethod(pure = false)
    public CompletionStage<Done> createRoleAssignment(User executor, RoleAssignment assignment) {
        return repository
            .findGrantedRoleAssignment(assignment)
            .thenCompose(maybeExisting -> {
                if (maybeExisting.isPresent()) {
                    return completed(Done.getInstance());
                } else {
                    var event = EventMetadata.apply(executor.getGUID());
                    var granted = GrantedRoleAssignment.apply(event, assignment);
                    return repository.insertOrUpdateGrantedRoleAssignment(granted);
                }
            });
    }

}
