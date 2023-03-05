package com.wellnr.platform.core.modules.users.ports;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.modules.users.exceptions.UserNotFoundException;
import com.wellnr.platform.core.modules.users.values.users.RegisteredUser;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface RegisteredUsersRepositoryPort {

    CompletionStage<List<RegisteredUser>> findRegisteredUsersByUsername(String username);

    CompletionStage<Optional<RegisteredUser>> findRegisteredUserByGUID(GUID id);

    CompletionStage<Optional<RegisteredUser>> findRegisteredUserByUserId(String id);

    default CompletionStage<RegisteredUser> getRegisteredUserById(GUID id) {
        return findRegisteredUserByGUID(id).thenApply(maybeUser ->
            maybeUser.orElseThrow(() -> UserNotFoundException.byGUID(id))
        );
    }

    CompletionStage<Done> updateOrInsertRegisteredUser(RegisteredUser user);

}
