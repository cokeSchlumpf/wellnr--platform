package com.wellnr.platform.core.modules.users.auth;

import com.wellnr.platform.core.modules.users.values.users.AnonymousUser;
import com.wellnr.platform.core.modules.users.values.users.User;
import io.javalin.http.Context;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * Chain multiple authentication handlers.
 * <p>
 * The registered authentication handlers will be called one after the other. If one handler returns
 * a different user than AnonymousUser, authentication is finished and following handlers are not called
 * anymore.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChainedAuthenticationHandler implements AuthenticationHandler {

    /**
     * List of authentication handlers to use. The handlers will be called
     * as ordered within the list.
     */
    List<AuthenticationHandler> handlers;

    /**
     * Creates a new instance.
     *
     * @param handler Handlers to be chained in order in which they are called.
     * @return A new instance.
     */
    public static ChainedAuthenticationHandler apply(AuthenticationHandler... handler) {
        var handlers = Arrays.stream(handler).toList();

        if (handlers.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one handler.");
        }

        return new ChainedAuthenticationHandler(handlers);
    }

    @Override
    public User handleAuthentication(Context ctx) {
        for (var handler : this.handlers) {
            var user = handler.handleAuthentication(ctx);

            if (!(user instanceof AnonymousUser)) {
                return user;
            }
        }

        // TODO mw: Get default roles for anonymous users.
        return AnonymousUser.apply(List.of());
    }

}
