package com.wellnr.platform.core.context;

import com.google.common.collect.Sets;
import com.wellnr.platform.common.databind.DefaultObjectMapperFactory;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.commands.Command;
import com.wellnr.platform.core.modules.PlatformModule;
import com.wellnr.platform.core.modules.users.UsersModule;
import com.wellnr.platform.core.modules.users.values.rbac.Role;
import io.javalin.Javalin;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class PlatformContextImpl implements PlatformContext {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformContext.class);

    private PlatformContext delegate;

    public static PlatformContextImpl apply() {
        var initializing = InitializingPlatformContext
            .apply()
            .withModule(UsersModule.apply())
            .withValue(DefaultObjectMapperFactory.apply())
            .withValue(Javalin.create());

        return new PlatformContextImpl(initializing);
    }

    @Override
    public synchronized PlatformContext initialize() {
        if (delegate instanceof InitializingPlatformContext init) {
            var commands = Sets.<Class<Command>>newHashSet();
            var roles = Sets.<Role>newHashSet();

            /*
             * Initialize all modules.
             */
            for (var module : init.modules.values()) {
                LOG.info("Initializing module `{}` ...", module.getName());

                commands.addAll(module.getCommands().values());
                roles.addAll(module.getRoles());

                module.init(this);
            }

            this.delegate = InitializedPlatformContext.apply(
                init.values, init.modules, commands, roles
            );

            return this;
        } else {
            throw new IllegalStateException("`initialize` cannot be called after initialization.");
        }
    }

    @Override
    public <T extends RootEntity> T getEntitySingleton(Class<T> entityType, T plainEntity) {
        return delegate.getEntitySingleton(entityType, plainEntity);
    }

    @Override
    public <T> PlatformContext withValue(T any, Class<? super T> clazz) {
        delegate.withValue(any, clazz);
        return this;
    }

    @Override
    public <T extends PlatformModule> PlatformContext withModule(T module, Class<T> clazz) {
        delegate.withModule(module, clazz);
        return this;
    }

    @Override
    public Set<Class<Command>> getCommands() {
        return delegate.getCommands();
    }

    @Override
    public <T extends PlatformModule> T getModule(Class<T> clazz) {
        return delegate.getModule(clazz);
    }

    @Override
    public Set<Role> getRoles() {
        return delegate.getRoles();
    }

    @Override
    public Optional<Role> findRoleByGUID(GUID id) {
        return delegate.findRoleByGUID(id);
    }

    @Override
    public <T> T getValue(Class<T> clazz) {
        return delegate.getValue(clazz);
    }

}
