package com.wellnr.platform.core.modules.users.auth;

import com.wellnr.platform.core.modules.users.values.users.AnonymousUser;
import com.wellnr.platform.core.modules.users.values.users.User;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Checks whether the request contains an authentication token which can be assigned to a user.
 * This mechanism might be used to authenticate users when working with the application via a CLI or via
 * other programmatic accesses where authentication via browser is not possible.
 */
@AllArgsConstructor(staticName = "apply")
public final class UserTokenAuthenticationHandler implements AuthenticationHandler {

    @Override
    public User handleAuthentication(Context ctx) {
        return AnonymousUser.apply(List.of());
    }

}
