package com.wellnr.platform.core.context;

import com.google.common.collect.Maps;
import com.wellnr.platform.common.async.AsyncBoundaryProxy;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.commands.Command;
import com.wellnr.platform.core.modules.PlatformModule;
import com.wellnr.platform.core.modules.users.values.rbac.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class InitializedPlatformContext implements PlatformContext {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformContext.class);

    private final Map<Class<?>, Object> values;

    private final Map<Class<? extends PlatformModule>, PlatformModule> modules;

    private final Set<Class<Command>> commands;

    private final Set<Role> roles;

    private final Map<GUID, RootEntity> entities;

    public static InitializedPlatformContext apply(
        Map<Class<?>, Object> values, Map<Class<? extends PlatformModule>, PlatformModule> modules,
        Set<Class<Command>> commands, Set<Role> roles
    ) {

        return new InitializedPlatformContext(
            Map.copyOf(values), Map.copyOf(modules), Set.copyOf(commands), Set.copyOf(roles), Maps.newHashMap()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T extends RootEntity> T getEntitySingleton(Class<T> entityType, T plainEntity) {
        if (entities.containsKey(plainEntity.getGUID())) {
            var existingEntity = entities.get(plainEntity.getGUID());

            if (entityType.isInstance(existingEntity)) {
                return (T) existingEntity;
            } else {
                throw new IllegalArgumentException(MessageFormat.format(
                    "Entity with GUID `{0}` already exists, but with different type. Existing type: `{1}`, requested type: `{2}``",
                    plainEntity.getGUID(), existingEntity.getClass(), entityType
                ));
            }
        } else {
            LOG.debug(
                "Creating new singleton entity instance for GUID `{}` of type `{}`",
                plainEntity.getGUID(), entityType
            );

            var newEntity = AsyncBoundaryProxy.createProxy(plainEntity, entityType);
            this.entities.put(newEntity.getGUID(), newEntity);

            return newEntity;
        }
    }

    @Override
    public <T> PlatformContext withSingletonInstance(T any, Class<? super T> clazz) {
        throw new IllegalStateException("`withValue` must not be called after initialization.");
    }

    @Override
    public <T extends PlatformModule> PlatformContext withModule(T module, Class<T> clazz) {
        throw new IllegalStateException("`withModule` must not be called after initialization.");
    }

    @Override
    public PlatformContext withModule(PlatformModule module) {
        throw new IllegalStateException("`withValue` must not be called after initialization.");
    }

    @Override
    public Set<Class<Command>> getCommands() {
        return commands;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PlatformModule> T getModule(Class<T> clazz) {
        if (modules.containsKey(clazz) && clazz.isInstance(modules.get(clazz))) {
            return (T) modules.get(clazz);
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                "No module registered for type `{0}`", clazz.getName()
            ));
        }
    }

    @Override
    public Set<Role> getRoles() {
        return this.roles;
    }

    @Override
    public Optional<Role> findRoleByGUID(GUID id) {
        return this.roles
            .stream()
            .filter(role -> role.getGUID().equals(id))
            .findFirst();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        if (values.containsKey(clazz) && clazz.isInstance(values.get(clazz))) {
            return (T) values.get(clazz);
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                "No module registered for type `{0}`", clazz.getName()
            ));
        }
    }
}
