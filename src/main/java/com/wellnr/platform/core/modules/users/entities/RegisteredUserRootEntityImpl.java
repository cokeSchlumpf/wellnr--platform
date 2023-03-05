package com.wellnr.platform.core.modules.users.entities;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.modules.users.ports.RegisteredUsersRepositoryPort;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletionStage;

import static com.wellnr.platform.common.Operators.completed;

@AllArgsConstructor(staticName = "apply")
public final class RegisteredUserRootEntityImpl implements RegisteredUserRootEntity {

    private final RegisteredUsersRepositoryPort repository;

    private final GUID guid;

    @Override
    public GUID getGUID() {
        return guid;
    }

    @Override
    public CompletionStage<Done> updateSettings(String displayName) {
        return repository
            .getRegisteredUserById(guid)
            .thenCompose(user -> {
                user = user.withDisplayName(displayName);
                return repository.updateOrInsertRegisteredUser(user);
            });
    }

    @Override
    public CompletionStage<Done> deleteUser() {
        // TODO mw: Implement.
        return completed(Done.getInstance());
    }

}
