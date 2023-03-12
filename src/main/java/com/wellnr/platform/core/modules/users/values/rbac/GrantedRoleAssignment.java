package com.wellnr.platform.core.modules.users.values.rbac;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.persistence.memento.HasMemento;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.modules.users.UsersModule;
import com.wellnr.platform.core.values.EventMetadata;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A granted role assignment
 */
@Value
@AllArgsConstructor(staticName = "apply")
public class GrantedRoleAssignment implements HasMemento<GrantedRoleAssignmentMemento> {

    private static final Logger LOG = LoggerFactory.getLogger(GrantedRoleAssignment.class);

    EventMetadata granted;

    RoleAssignment assignment;

    GUID guid;

    public static GrantedRoleAssignment apply(EventMetadata granted, RoleAssignment assignment) {
        var id = MessageFormat
            .format(
                "{0}{1}{2}",
                assignment.getAssignedTo(), assignment.getRole().getGUID(), assignment.getSubject())
            .hashCode();

        var guid = GUID.fromString(MessageFormat.format(
            "{0}/roles-assignments/{1}",
            UsersModule.GUID_PREFIX, id
        ));

        return apply(granted, assignment, guid);
    }

    /**
     * Factory method to generate instance from Memento.
     *
     * @param ctx     The platform instance's context.
     * @param memento The Memento.
     * @return A new instance.
     */
    public static CompletionStage<GrantedRoleAssignment> fromMemento(PlatformContext ctx,
                                                                     GrantedRoleAssignmentMemento memento) {
        var role = ctx
            .findRoleByGUID(memento.getRole())
            .orElseGet(() -> {
                LOG.warn(MessageFormat.format(
                    "No role registered with GUID `{0}`. The role will be mapped to a role with empty permissions.",
                    memento.getRole()
                ));

                return Role.apply(memento.getRole(), Set.of());
            });

        var assignment = RoleAssignment.apply(
            memento.getAssignedTo(), memento.getSubject(), role);

        var grantedAssignment = GrantedRoleAssignment.apply(memento.getGranted(), assignment);

        return CompletableFuture.completedFuture(grantedAssignment);
    }

    @Override
    public GrantedRoleAssignmentMemento getMemento() {
        return GrantedRoleAssignmentMemento.apply(
            granted, assignment.getAssignedTo(), assignment.getSubject(),
            assignment.getRole().getGUID());
    }

    @Override
    public GUID getGUID() {
        return guid;
    }
}
