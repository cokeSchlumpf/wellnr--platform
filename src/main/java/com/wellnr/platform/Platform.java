package com.wellnr.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.wellnr.platform.common.Templates;
import com.wellnr.platform.common.databind.DefaultObjectMapperFactory;
import com.wellnr.platform.common.databind.ObjectMapperFactory;
import com.wellnr.platform.common.functions.Procedure1;
import com.wellnr.platform.core.config.PlatformConfiguration;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.modules.core.CoreModule;
import com.wellnr.platform.core.modules.users.UsersModule;
import com.wellnr.platform.core.modules.users.ports.RegisteredUsersRepositoryPort;
import com.wellnr.platform.core.modules.users.values.users.RegisteredUser;
import com.wellnr.platform.core.persistence.inmemory.InMemoryRepository;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor(staticName = "apply")
public class Platform {

    private static final Logger LOG = LoggerFactory.getLogger(Platform.class);

    private final PlatformContext context;

    /**
     * Creates a new Platform instance.
     *
     * @return The new instance.
     */
    public static Platform apply() {
        var context = PlatformContext
            .apply()
            .withSingletonInstance(PlatformConfiguration.apply(), PlatformConfiguration.class)
            .withSingletonInstance(DefaultObjectMapperFactory.apply(), ObjectMapperFactory.class)
            .withSingletonInstance(Javalin.create(), Javalin.class)
            .withModuleFromContext(CoreModule::apply, CoreModule.class)
            .withModuleFromContext(UsersModule::apply, UsersModule.class);

        return Platform.apply(context);
    }

    /**
     * Use this method to configure the platform prior start.
     *
     * @param configFn A function which receives prior configuration and returns new.
     */
    public void configure(Procedure1<PlatformContext> configFn) {
        configFn.run(this.context);
    }

    /**
     * Initialize and start the Platform app.
     */
    public void start() {
        this.context.initialize();

        var config = this
            .context
            .getInstance(PlatformConfiguration.class);

        this
            .context
            .getInstance(Javalin.class)
            .updateConfig(cfg -> cfg.jsonMapper(
                new JavalinJackson(this.context.getInstance(ObjectMapper.class))
            ))
            .start(config.getHost(), config.getPort());

        Runtime
            .getRuntime()
            .addShutdownHook(new Thread(this::stop));

        showBanner();
    }

    public void stop() {
        this.context.stop();
        this.context.getInstance(Javalin.class).stop();
    }

    private void showBanner() {
        var map = Maps.<String, Object>newHashMap();
        var config = this.context.getInstance(PlatformConfiguration.class);

        map.put(
            "version", config.getVersion()
        );

        map.put(
            "environment", config.getEnvironment()
        );

        var banner = Templates.renderTemplateFromResources(
            config.getBanner(), map
        );

        LOG.info(
            "{} has started {}", config.getName(), banner
        );
    }

}
