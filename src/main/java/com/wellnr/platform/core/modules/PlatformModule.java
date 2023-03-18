package com.wellnr.platform.core.modules;

import com.google.common.collect.Maps;
import com.wellnr.platform.core.commands.Command;
import com.wellnr.platform.core.modules.users.values.rbac.Role;

import java.util.Map;
import java.util.Set;

/**
 * Abstract interface for definition of Platform modules.
 *
 * Modules provide functionalities offered by the platform (REST endpoints, services, etc.).
 */
public interface PlatformModule {

    /**
     * Technical name of the module.
     *
     * @return The name, obviously.
     */
    String getName();

    /**
     * Will be called during start-up of Platform.
     */
    default void init() {
        // do nothing by default
    }

    /**
     * Will be called during shutdown of Platform.
     */
    default void stop() {
        // do nothing by default
    }

    default Map<String, Class<Command>> getCommands() {
        return Maps.newHashMap();
    }

    /**
     * Return the set of roles defined by this module.
     *
     * @return A set of module-specific roles.
     */
    default Set<Role> getRoles() {
        return Set.of();
    }

    /**
     * Returns a numeric value which will be used to define initialisation order of modules.
     *
     * By default, core modules are set to `0`. Custom modules to `10`. This ensures that core modules
     * are initialized before custom modules without specifying separate dependencies.
     *
     * @return The numeric value.
     */
    default int getInitStage() {
        return 10;
    }

}
