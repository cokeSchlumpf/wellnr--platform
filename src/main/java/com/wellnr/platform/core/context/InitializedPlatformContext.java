package com.wellnr.platform.core.context;

import com.google.common.collect.Maps;
import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.async.AsyncBoundaryProxy;
import com.wellnr.platform.common.functions.Function0;
import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.commands.Command;
import com.wellnr.platform.core.modules.PlatformModule;
import com.wellnr.platform.core.modules.users.values.rbac.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class InitializedPlatformContext implements PlatformContextInternal {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformContext.class);

    private final Map<Class<?>, Object> instances;

    private final Map<Class<? extends PlatformModule>, PlatformModule> modules;

    private final Map<String, Class<Command>> commands;

    private final Set<Role> roles;

    private final Map<GUID, RootEntity> entities;

    public static InitializedPlatformContext apply(
        Map<Class<?>, Object> values, Map<Class<? extends PlatformModule>, PlatformModule> modules,
        Map<String, Class<Command>> commands, Set<Role> roles
    ) {

        return new InitializedPlatformContext(
            Map.copyOf(values), Map.copyOf(modules), Map.copyOf(commands), Set.copyOf(roles), Maps.newHashMap()
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
                    "Entity with GUID `{0}` already exists, but with different type. Existing type: `{1}`, requested " +
                        "type: `{2}``",
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

    public Map<String, Class<Command>> getCommands() {
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
    public List<PlatformModule> getModules() {
        return List.copyOf(modules.values());
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T extends RootEntity> T getOrCreateEntity(Class<T> entityType, GUID guid, Function1<GUID, T> createInstance) {
        if (!this.entities.containsKey(guid)) {
            var entity = createInstance.get(guid);

            if (!entity.getGUID().equals(guid)) {
                throw new IllegalStateException(MessageFormat.format(
                    "Returned entity must have the specified GUID `{0}`", guid
                ));
            }

            var asyncEntity = AsyncBoundaryProxy.createProxy(entity, entityType);
            this.entities.put(guid, asyncEntity);
        }

        var entity = this.entities.get(guid);

        if (!entityType.isInstance(entity)) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Another entity with entity GUID `{0}`, but with different type `{1}` " +
                "has been already registered. Please ensure that GUID of different entity types do not overlap.",
                guid, entity.getClass().getName()
            ));
        }

        return (T) entity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T extends RootEntity> T getOrCreateEntity(Class<T> entityType, Function0<T> createInstance) {
        var maybeExistingEntity = this
            .entities
            .values()
            .stream()
            .filter(entityType::isInstance)
            .findFirst();

        return Operators
            .when(maybeExistingEntity.isEmpty())
            .then(() -> {
                var entity = createInstance.get();
                var asyncEntity = AsyncBoundaryProxy.createProxy(entity, entityType);

                this.entities.put(entity.getGUID(), asyncEntity);

                return asyncEntity;
            })
            .otherwise(
                () -> (T) maybeExistingEntity.orElseThrow()
            );
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
    public Map<Class<?>, Object> getInstances() {
        return this.instances;
    }
}
