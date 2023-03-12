package com.wellnr.platform.core.context;

import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.commands.Command;
import com.wellnr.platform.core.modules.PlatformModule;
import com.wellnr.platform.core.modules.users.values.rbac.Role;

import java.util.Optional;
import java.util.Set;

public interface PlatformContext {

    static PlatformContext apply() {
        return PlatformContextImpl.apply();
    }

    /**
     * Initializes the context (and the application).
     *
     * @return The platform context instance.
     */
    default  PlatformContext initialize() {
        return this;
    }

    /**
     * Get the singleton entity instance for a given "plain" entity. The context will return the entity (or
     * a previous registered entity with the same identity) wrapped within an
     * {@link com.wellnr.platform.common.async.AsyncBoundaryProxy}.
     *
     * This ensures that calls to an entity do never overlap and consistency of an entity is ensured.
     *
     * @param <T> The type of the entity.
     * @param entityType The type if the entity.
     * @param plainEntity The plain entity instance.
     * @return The entity singleton.
     */
    <T extends RootEntity> T getEntitySingleton(Class<T> entityType, T plainEntity);

    /**
     * Register any object in the context during platform initialisation. Ensure that for every type (class)
     * the context can only have a single context value registered.
     * <p>
     * This method will use the objects class to register the value.
     *
     * @param any The value to be registered.
     * @return The instance of the platform context to chain calls.
     */
    @SuppressWarnings("unchecked")
    default <T> PlatformContext withSingletonInstance(T any) {
        return this.withSingletonInstance(any, (Class<T>) any.getClass());
    }

    /**
     * Register any object in the context during platform initialisation. Ensure that for every type (class)
     * the context can only have a single context value registered.
     *
     * @param any   The value to be registered.
     * @param clazz The class for which the value should be registered.
     * @param <T>   The type of the value.
     * @return The instance of the platform context to chain calls.
     */
    <T> PlatformContext withSingletonInstance(T any, Class<? super T> clazz);

    /**
     * Register a platform module during initialisation of the application.
     *
     * @param module The module to be registered.
     * @param clazz The type of the module.
     * @return The instance of the platform context to chain calls.
     */
    <T extends PlatformModule> PlatformContext withModule(T module, Class<T> clazz);

    /**
     * Register a platform module during initialisation of the application.
     *
     * @param module The module to be registered.
     * @return The instance of the platform context to chain calls.
     */
    @SuppressWarnings("unchecked")
    default <T extends PlatformModule> PlatformContext withModule(T module) {
        return this.withModule(module, (Class<T>) module.getClass());
    }

    /**
     * Returns a set of all registered commands of the application.
     *
     * @return The set of commands.
     */
    Set<Class<Command>> getCommands();

    /**
     * Get a registered module after context has been initialized.
     *
     * @param clazz The class of the module.
     * @param <T>   The type of the module.
     * @return The initialized module instance.
     */
    <T extends PlatformModule> T getModule(Class<T> clazz);

    /**
     * Get all available roles which have been registered in the platform instance.
     *
     * @return The set of available roles.
     */
    Set<Role> getRoles();

    /**
     * Returns a Role by its GUID, if it exists.
     *
     * @return The role or none.
     */
    Optional<Role> findRoleByGUID(GUID id);

    /**
     * Get a value after initialisation.
     *
     * @param clazz The class/ type which has been used to register the value.
     * @param <T>   The type of the value.
     * @return The value, if registered.
     */
    <T> T getInstance(Class<T> clazz);

}
