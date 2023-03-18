package com.wellnr.platform.core.modules.users.ports;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.core.modules.users.exceptions.UserNotFoundException;
import com.wellnr.platform.core.modules.users.values.users.RegisteredUser;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface RegisteredUsersRepositoryPort {

    CompletionStage<List<RegisteredUser>> findAllRegisteredUsersByUsername(String username);

    CompletionStage<Optional<RegisteredUser>> findOneRegisteredUserByGUID(GUID id);

    CompletionStage<Optional<RegisteredUser>> findOneRegisteredUserByUserId(String id);

    default CompletionStage<RegisteredUser> getRegisteredUserById(GUID id) {
        return findOneRegisteredUserByGUID(id).thenApply(maybeUser ->
            maybeUser.orElseThrow(() -> UserNotFoundException.byGUID(id))
        );
    }

    CompletionStage<Done> insertOrUpdateRegisteredUser(RegisteredUser user);

}
