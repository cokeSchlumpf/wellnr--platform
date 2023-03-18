package com.wellnr.platform.core.modules.core.resources;

import com.wellnr.platform.common.config.Configs;
import com.wellnr.platform.core.config.PlatformConfiguration;
import com.wellnr.platform.core.context.PlatformContext;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiResponse;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor(staticName = "apply")
public final class AboutResource {

    PlatformContext context;

    @Value
    @AllArgsConstructor(staticName = "apply")
    public static class About {

        String environment;

        String version;

    }

    @OpenApi(
        tags = "Core",
        path = "/api/about",
        methods = HttpMethod.GET,
        summary = "Application instance metadata",
        description = "Display meta information about the application instance.",
        responses = {
            @OpenApiResponse(status = "200", description = "Application instance metadata", content = {
                @OpenApiContent(from = About.class)
            })
        }
    )
    public void getAbout(Context ctx) {
        var config = this.context.getInstance(PlatformConfiguration.class);

        ctx.json(
            About.apply(
                config.getEnvironment(), config.getVersion()
            )
        );
    }

    @OpenApi(
        tags = "Core",
        path = "api/core/configuration",
        summary = "Application Configuration",
        description = "Returns all available configurations for the environment/ instance.",
        responses = {
            @OpenApiResponse(status = "200", description = "Configuration values", content = {
                @OpenApiContent(from = String.class)
            })
        }
    )
    public void getConfiguration(Context ctx) {
        ctx.result(Configs.asString(Configs.application));
    }

}
