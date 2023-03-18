package com.wellnr.platform.core.modules.users.entities;

import com.wellnr.platform.common.async.AsyncMethod;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.context.RootEntity;
import com.wellnr.platform.core.modules.users.values.users.RegisteredUser;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface RegisteredUsersRootEntity extends RootEntity {

    /**
     * Search for a registered user by the user's external identity provider id.
     *
     * @param userId The id as sent by the identity provider.
     * @return The registered user or nothing.
     */
    @AsyncMethod(pure = true)
    CompletionStage<Optional<RegisteredUser>> findByExternalUserId(String userId);

    /**
     * Create a new user account.
     *
     * @param userId The username of the user, as provided by Identity Provider.
     * @param displayName How the user wants to be displayed.
     * @return Done.
     */
    @AsyncMethod(pure = false)
    CompletionStage<Done> registerUser(String userId, String displayName);
}
