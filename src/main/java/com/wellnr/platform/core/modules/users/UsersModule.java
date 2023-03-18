package com.wellnr.platform.core.modules.users;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.core.config.RepositoryMode;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.modules.PlatformModule;
import com.wellnr.platform.core.modules.users.auth.AuthenticatedUserAuthenticationHandler;
import com.wellnr.platform.core.modules.users.auth.AuthenticationHandler;
import com.wellnr.platform.core.modules.users.auth.ChainedAuthenticationHandler;
import com.wellnr.platform.core.modules.users.auth.UserTokenAuthenticationHandler;
import com.wellnr.platform.core.modules.users.configuration.UsersConfiguration;
import com.wellnr.platform.core.modules.users.entities.RegisteredUsersRootEntity;
import com.wellnr.platform.core.modules.users.entities.RegisteredUsersRootEntityImpl;
import com.wellnr.platform.core.modules.users.ports.RegisteredUsersRepositoryPort;
import com.wellnr.platform.core.modules.users.values.resources.AboutResource;
import com.wellnr.platform.core.modules.users.values.users.RegisteredUser;
import com.wellnr.platform.core.persistence.inmemory.InMemoryRepository;
import com.wellnr.platform.core.persistence.mongo.MongoRepository;
import io.javalin.Javalin;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UsersModule implements PlatformModule {

    public static String NAME = "users";

    public static String GUID_PREFIX = "/app/modules/users";

    private final PlatformContext context;

    public static UsersModule apply(
        PlatformContext context,
        RegisteredUsersRepositoryPort usersRepository
    ) {
        // add to context to enable injection later.
        context.withSingletonInstance(usersRepository, RegisteredUsersRepositoryPort.class);

        return new UsersModule(context);
    }

    public static UsersModule apply(PlatformContext context) {
        var config = UsersConfiguration.apply();

        var repo = Operators
            .when(
                config.getMode().equals(RepositoryMode.in_memory)
            )
            .then(
                () -> InMemoryRepository.create(context, RegisteredUsersRepositoryPort.class, RegisteredUser.class)
            )
            .otherwise(
                () -> MongoRepository.create(
                    context, RegisteredUsersRepositoryPort.class, config.getDatabase(), RegisteredUser.class
                )
            );

        return apply(context, repo);
    }

    @Override
    public void init() {
        var javalin = context.getInstance(Javalin.class);
        var about = AboutResource.apply();

        context.withSingletonInstance(
            ChainedAuthenticationHandler.apply(
                AuthenticatedUserAuthenticationHandler.apply(context),
                UserTokenAuthenticationHandler.apply()
            ),
            AuthenticationHandler.class
        );

        javalin
            .before(ctx -> {
                var handler = context.getInstance(AuthenticationHandler.class);
                var user = handler.handleAuthentication(ctx);
                ctx.attribute("user", user);
            })
            .get("/api/users/about", about::getAbout);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getInitStage() {
        return 0;
    }

    public RegisteredUsersRootEntity getRegisteredUsers() {
        return context.getOrCreateEntity(
            RegisteredUsersRootEntity.class,
            () -> context.createFromContext(RegisteredUsersRootEntityImpl.class));
    }
}
