package com.wellnr.platform.core.modules.users.entities;

import com.wellnr.platform.common.async.AsyncMethod;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.context.RootEntity;
import com.wellnr.platform.core.modules.users.ports.RegisteredUsersRepositoryPort;
import lombok.AllArgsConstructor;

import java.util.concurrent.CompletionStage;

import static com.wellnr.platform.common.Operators.completed;

@AllArgsConstructor(staticName = "apply")
public class RegisteredUserRootEntity implements RootEntity {

    private final RegisteredUsersRepositoryPort repository;

    private final GUID guid;

    @Override
    @AsyncMethod(pure = true)
    public GUID getGUID() {
        return guid;
    }

    @AsyncMethod(pure = false)
    public CompletionStage<Done> updateSettings(String displayName) {
        return repository
            .getRegisteredUserById(guid)
            .thenCompose(user -> {
                user = user.withDisplayName(displayName);
                return repository.insertOrUpdateRegisteredUser(user);
            });
    }

    @AsyncMethod(pure = false)
    public CompletionStage<Done> delete() {
        // TODO mw: Implement.
        return completed(Done.getInstance());
    }

}
