package com.wellnr.platform.core.modules.core;

import com.wellnr.platform.core.config.PlatformConfiguration;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.modules.PlatformModule;
import com.wellnr.platform.core.modules.core.resources.AboutResource;
import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;

@AllArgsConstructor(staticName = "apply")
public class CoreModule implements PlatformModule {

    public static final String NAME = "core";

    private final PlatformContext context;

    @Override
    public void init() {
        var javalin = context.getInstance(Javalin.class);
        var config = context.getInstance(PlatformConfiguration.class);

        var aboutResource = AboutResource.apply(context);
        var docsPath = "/api/openapi";

        javalin
            .updateConfig(cfg -> {
                cfg.showJavalinBanner = false;

                cfg.plugins.register(new OpenApiPlugin(
                    new OpenApiPluginConfiguration()
                        .withDocumentationPath(docsPath)
                        .withDefinitionConfiguration((version, definition) -> definition
                            .withOpenApiInfo(info -> {
                                info.setTitle(config.getName());
                                info.setVersion(config.getVersion());
                                info.setDescription(config.getDescription());
                            })
                            .withServer(server -> {
                                server.setUrl(MessageFormat.format(
                                    "http://localhost:{0}/",
                                    String.valueOf(config.getPort())
                                ));
                                server.setDescription("current instance");
                            })
                        )
                ));

                SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
                swaggerConfiguration.setDocumentationPath(docsPath);
                swaggerConfiguration.setUiPath("/api/docs");
                cfg.plugins.register(new SwaggerPlugin(swaggerConfiguration));
            })
            .get("api/about", aboutResource::getAbout)
            .get("api/core/configuration", aboutResource::getConfiguration);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getInitStage() {
        return 0;
    }
}
