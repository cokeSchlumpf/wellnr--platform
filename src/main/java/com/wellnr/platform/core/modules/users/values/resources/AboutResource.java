package com.wellnr.platform.core.modules.users.values.resources;

import com.wellnr.platform.core.config.PlatformConfiguration;
import com.wellnr.platform.core.modules.users.values.users.User;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor(staticName = "apply")
public final class AboutResource {

    @OpenApi(
        tags = "Users",
        path = "/api/users/about",
        methods = HttpMethod.GET,
        summary = "Current user information",
        description = "Display information of the currently logged in user. This endpoint is helpful to test login/ " +
            "authentication mechanisms.",
        headers = @OpenApiParam(
            name = "x-user-id",
            description = "The user id of the authenticated user.",
            allowEmptyValue = true
        ),
        responses = {
            @OpenApiResponse(status = "200", description = "Current user information.", content = {
                @OpenApiContent(from = UserInformation.class)
            })
        }
    )
    public void getAbout(Context ctx) {
        var user = (User) ctx.attribute("user");
        ctx.json(UserInformation.apply(user));
    }

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static class UserInformation {

        User user;

    }

}
