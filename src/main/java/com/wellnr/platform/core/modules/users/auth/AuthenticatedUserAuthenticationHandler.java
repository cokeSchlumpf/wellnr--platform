package com.wellnr.platform.core.modules.users.auth;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.core.config.PlatformConfiguration;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.modules.users.values.users.AnonymousUser;
import com.wellnr.platform.core.modules.users.values.users.AuthenticatedUser;
import com.wellnr.platform.core.modules.users.values.users.User;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Authentication handler which checks if user information got injected from external identity provider.
 * The external identity provider might have been called in a proxy node which processed the HTTP request
 * prior arriving at the application server.
 */
@AllArgsConstructor(staticName = "apply")
public final class AuthenticatedUserAuthenticationHandler implements AuthenticationHandler {

    private final PlatformContext context;

    @Override
    public User handleAuthentication(Context ctx) {
        var config = context.getInstance(PlatformConfiguration.class);
        var userIdHeaderName = config.getUserIdHeaderName();
        // var userRolesHeaderName = config.getUserRolesHeaderName();

        var headers = ctx.headerMap();

        // Currently not required ...
        //
        // var roles = Operators
        //     .when(headers.containsKey(userRolesHeaderName))
        //     .then(() -> Arrays
        //         .stream(
        //             headers.get(userRolesHeaderName).split(",")
        //         )
        //         .toList()
        //     )
        //     .otherwise(List::of);

        return Operators
            .when(headers.containsKey(userIdHeaderName))
            .then(() -> {
                var userId = headers.get(userIdHeaderName);
                return (User) AuthenticatedUser.apply(userId, List.of());
            })
            .otherwise(() -> AnonymousUser.apply(List.of()));
    }

}
