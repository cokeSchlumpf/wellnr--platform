package com.wellnr.platform.core.modules.users.auth;

import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.modules.users.values.users.User;
import io.javalin.http.Context;

import java.util.Optional;

public interface AuthenticationHandler {

    /**
     * Should return user authentication details, based on the request. Usually user information should have been
     * injected into the request header.
     *
     * @param ctx     The request context.
     * @return The authenticated user object, might be {@link com.wellnr.platform.core.modules.users.values.users.AnonymousUser} if no user
     * information is included in request.
     */
    User handleAuthentication(Context ctx);

}
