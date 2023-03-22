package com.wellnr.platform.core.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.ReflectionUtils;
import com.wellnr.platform.common.databind.ObjectMapperFactory;
import com.wellnr.platform.common.functions.Function0;
import com.wellnr.platform.common.functions.Function1;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.commands.Command;
import com.wellnr.platform.core.config.PlatformConfiguration;
import com.wellnr.platform.core.modules.PlatformModule;
import com.wellnr.platform.core.modules.users.values.rbac.Role;
import io.javalin.Javalin;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class PlatformContextImpl implements PlatformContext {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformContext.class);

    private PlatformContextInternal delegate;

    public static PlatformContextImpl apply() {
        var initializing = InitializingPlatformContext.apply();
        return new PlatformContextImpl(initializing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createFromContextWithArgs(Class<? extends T> classToCreate, Map<Class<?>, Object> additionalContext) {
        /*
         * Find a suitable factory method.
         */
        var annotatedFactory = ReflectionUtils.getMethod(
            classToCreate, CreateFromContextCreator.class, classToCreate, m -> Modifier.isStatic(m.getModifiers())
        );

        var defaultFactory = ReflectionUtils.getMethod(
            classToCreate, null, classToCreate, m -> Modifier.isStatic(m.getModifiers()) && m.getName().equals("apply")
        );

        var factory = annotatedFactory.orElse(defaultFactory.orElseThrow(
            () -> new IllegalArgumentException(MessageFormat.format(
                "The class `{0}` does not provide a suitable factory method. Please specify a static factory method " +
                    "with name `apply` or annotate another factory method with `@CreateFromContextCreator`.",
                classToCreate.getName()
            ))
        ));

        /*
         * Check whether
         */
        var parameters = Arrays
            .stream(factory.getParameters())
            .map(param -> {
                var valueFromContext = this.findInstance(param.getType());
                var valueFromAdditionalContext = Optional.ofNullable(additionalContext.get(param.getType()));

                var value = Optional.ofNullable(
                    valueFromAdditionalContext.orElseGet(() -> valueFromContext.orElse(null))
                );

                if (value.isPresent()) {
                    return value.get();
                } else {
                    throw new IllegalArgumentException(MessageFormat.format(
                        "Unable to create instance for class `{0}`. Did not find a suitable value for parameter `{1}`" +
                            " of type `{2}` within context.",
                        classToCreate.getName(), param.getName(), param.getType()
                    ));
                }
            })
            .toArray();

        return (T) Operators.suppressExceptions(() -> factory.invoke(null, parameters));
    }

    @Override
    public synchronized void initialize() {
        if (delegate instanceof InitializingPlatformContext init) {
            var commands = Maps.<String, Class<Command>>newHashMap();
            var roles = Sets.<Role>newHashSet();

            /*
             * Instantiate object mapper.
             */
            var omf = this.delegate.getInstance(ObjectMapperFactory.class);
            this.delegate.withSingletonInstance(
                omf.createJsonMapper(true), ObjectMapper.class
            );

            /*
             * Initialize all modules.
             */
            var modulesOrdered = init
                .modules
                .values()
                .stream()
                .sorted(Comparator.comparing(PlatformModule::getInitStage))
                .toList();

            for (var module : modulesOrdered) {
                LOG.info("Initializing module `{}` ...", module.getName());

                commands.putAll(module.getCommands());
                roles.addAll(module.getRoles());

                module.init();
            }

            /*
             * Register commands.
             */
            commands.forEach((cmd, command) -> {
                var type = new NamedType(command, cmd);
                delegate.getInstance(ObjectMapper.class).registerSubtypes(type);
            });

            this.delegate = InitializedPlatformContext.apply(
                init.instances, init.modules, commands, roles
            );

        } else {
            throw new IllegalStateException("`initialize` cannot be called after initialization.");
        }
    }

    @Override
    public <T extends RootEntity> T getEntitySingleton(Class<T> entityType, T plainEntity) {
        return delegate.getEntitySingleton(entityType, plainEntity);
    }

    @Override
    public <T> PlatformContext withSingletonInstance(T any, Class<? super T> clazz) {
        delegate.withSingletonInstance(any, clazz);
        return this;
    }

    @Override
    public <T extends PlatformModule> PlatformContext withModule(T module, Class<T> clazz) {
        delegate.withModule(module, clazz);
        return this;
    }

    @Override
    public <T extends PlatformModule> T getModule(Class<T> clazz) {
        return delegate.getModule(clazz);
    }

    @Override
    public List<PlatformModule> getModules() {
        return delegate.getModules();
    }

    @Override
    public <T extends RootEntity> T getOrCreateEntity(Class<T> entityType, GUID guid, Function1<GUID, T> createInstance) {
        return delegate.getOrCreateEntity(entityType, guid, createInstance);
    }

    @Override
    public <T extends RootEntity> T getOrCreateEntity(Class<T> entityType, Function0<T> createInstance) {
        return delegate.getOrCreateEntity(entityType, createInstance);
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
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        var instances = delegate.getInstances();

        if (instances.containsKey(clazz) && clazz.isInstance(instances.get(clazz))) {
            return (T) instances.get(clazz);
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                "No module registered for type `{0}`", clazz.getName()
            ));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> findInstance(Class<T> clazz) {
        var instances = delegate.getInstances();

        if (instances.containsKey(clazz) && clazz.isInstance(instances.get(clazz))) {
            return Optional.of((T) instances.get(clazz));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void stop() {
        var config = this.getInstance(PlatformConfiguration.class);
        var server = this.getInstance(Javalin.class);

        LOG.info("Stopping {} ...", config.getName());

        if (Objects.nonNull(server)) {
            server.stop();
        }

        this
            .getModules()
            .forEach(module -> {
                LOG.info("Stopping module `{}`", module.getName());
                module.stop();
            });

        LOG.info("{} has stopped", config.getName());

    }
}
