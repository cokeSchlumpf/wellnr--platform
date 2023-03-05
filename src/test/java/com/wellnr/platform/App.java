package com.wellnr.platform;

import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.redoc.ReDocConfiguration;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;

public class App {

    public static void main(String... args) {
        Javalin.create(config -> {
            var openApiConfiguration = new OpenApiPluginConfiguration()
                .withDefinitionConfiguration((s, definition) -> {
                    definition.withOpenApiInfo(info -> {
                        info.setTitle("Hello World");
                    });
                });

            config.plugins.register(new OpenApiPlugin(openApiConfiguration));

            SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
            config.plugins.register(new SwaggerPlugin(swaggerConfiguration));

            ReDocConfiguration reDocConfiguration = new ReDocConfiguration();
            config.plugins.register(new ReDocPlugin(reDocConfiguration));
        })
        .get("/", ctx -> ctx.result("Hello World!"))
        .start(7002);
    }

}
