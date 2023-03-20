package com.wellnr.platform.core.modules.users.entities;

import com.wellnr.platform.common.async.AsyncMethod;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.context.RootEntity;
import com.wellnr.platform.core.modules.users.UsersModule;
import com.wellnr.platform.core.modules.users.exceptions.UserAlreadyRegisteredException;
import com.wellnr.platform.core.modules.users.ports.RegisteredUsersRepositoryPort;
import com.wellnr.platform.core.modules.users.values.users.RegisteredUser;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

@AllArgsConstructor(staticName = "apply")
public class RegisteredUsersRootEntity implements RootEntity {

    public static final GUID ENTITIES_GUID = GUID.fromString(MessageFormat.format(
        "{0}/registered-users",
        UsersModule.GUID_PREFIX
    ));

    RegisteredUsersRepositoryPort repository;

    @Override
    @AsyncMethod(pure = true)
    public GUID getGUID() {
        return ENTITIES_GUID;
    }

    @AsyncMethod(pure = true)
    public CompletionStage<Optional<RegisteredUser>> findByExternalUserId(String externalUserId) {
        return repository.findOneRegisteredUserByExternalUserId(externalUserId);
    }

    @AsyncMethod(pure = false)
    public CompletionStage<Done> registerUser(String userId, String displayName) {
        return repository
            .findOneRegisteredUserByExternalUserId(userId)
            .thenCompose(maybeUser -> {
                if (maybeUser.isPresent()) {
                    throw UserAlreadyRegisteredException.byUserId(userId);
                } else {
                    // TODO mw: Get initial Roles from configuration.
                    var registeredUser = RegisteredUser.apply(userId, displayName, Collections.emptyList());

                    return repository.insertOrUpdateRegisteredUser(registeredUser);
                }
            });
    }

}
