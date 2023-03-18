package com.wellnr.platform.core.context;

import com.google.common.collect.Maps;
import com.wellnr.platform.common.functions.Function0;
import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.commands.Command;
import com.wellnr.platform.core.modules.PlatformModule;
import com.wellnr.platform.core.modules.users.values.rbac.Role;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor(staticName = "apply")
final class InitializingPlatformContext implements PlatformContextInternal {

    final Map<Class<?>, Object> instances;

    final Map<Class<? extends PlatformModule>, PlatformModule> modules;

    public static InitializingPlatformContext apply() {
        return apply(Maps.newHashMap(), Maps.newHashMap());
    }

    @Override
    public synchronized  <T extends RootEntity> T getEntitySingleton(Class<T> entityType, T plainEntity) {
        throw new IllegalStateException("`getEntitySingleton` must not be called during system initialization.");
    }

    @Override
    public <T> PlatformContext withSingletonInstance(T any, Class<? super T> clazz) {
        this.instances.put(clazz, any);
        return this;
    }

    @Override
    public <T extends PlatformModule> PlatformContext withModule(T module, Class<T> clazz) {
        this.modules.put(clazz, module);
        return this;
    }

    @Override
    public Set<Class<Command>> getCommands() {
        throw new IllegalStateException("`getCommands` must not be called during system initialization.");
    }

    @Override
    public <T extends PlatformModule> T getModule(Class<T> clazz) {
        throw new IllegalStateException("`getModule` must not be called during system initialization.");
    }

    @Override
    public List<PlatformModule> getModules() {
        throw new IllegalStateException("`getModules` must not be called during system initialization.");
    }

    @Override
    public <T extends RootEntity> T getOrCreateEntity(Class<T> entityType, GUID guid, Function1<GUID, T> createInstance) {
        throw new IllegalStateException("`getOrCreateEntity` must not be called during system initialization.");
    }

    @Override
    public <T extends RootEntity> T getOrCreateEntity(Class<T> entityType, Function0<T> createInstance) {
        throw new IllegalStateException("`getOrCreateEntity` must not be called during system initialization.");
    }

    @Override
    public Set<Role> getRoles() {
        throw new IllegalStateException("`getRoles` must not be called during system initialization.");
    }

    @Override
    public Optional<Role> findRoleByGUID(GUID id) {
        throw new IllegalStateException("`getRoleByGUID` must not be called during system initialization.");
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        throw new IllegalStateException("`getInstance` must not be called during system initialization.");
    }

    @Override
    public Map<Class<?>, Object> getInstances() {
        return this.instances;
    }
}
