package com.wellnr.platform.core.modules.core.resources;

import com.wellnr.platform.core.commands.Command;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.modules.users.values.users.User;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor(staticName = "apply")
public class CommandResource {

    private final PlatformContext context;

    /**
     * Handle execution of command requests.
     */
    @OpenApi(
        tags = "Core",
        path = "/api/commands",
        methods = HttpMethod.POST,
        summary = "Execute commands.",
        description = "Single endpoint to send commands to the application.",
        requestBody = @OpenApiRequestBody(
            content = @OpenApiContent(from = Command.class),
            required = true
        ),
        responses = {
            @OpenApiResponse(status = "200", description = "Application instance metadata", content = {
                @OpenApiContent(from = AboutResource.About.class)
            })
        }
    )

    public void executeCommand(Context ctx) {
        var command = ctx.bodyAsClass(Command.class);
        var user = (User) Objects.requireNonNull(ctx.attribute("user"));

        var result = command
            .run(user, context)
            .toCompletableFuture();

        var acceptRaw = ctx.header("Accept");
        var accept = acceptRaw != null ? acceptRaw : "application/json";

        if (accept.equals("text/plain")) {
            ctx.future(
                () -> result
                    .thenApply(r -> r.toPlainText(context))
                    .thenAccept(ctx::result)
            );
        } else {
            ctx.future(
                () -> result.thenAccept(ctx::json)
            );
        }
    }

}
