package com.wellnr.platform.core.persistence.memento;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.functions.Function2;
import com.wellnr.platform.core.context.PlatformContext;

import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Mementos {

    private Mementos() {

    }

    @SuppressWarnings("unchecked")
    public static <T extends HasMemento<?>> Function2<PlatformContext, Object, CompletionStage<T>> createFromMementoFunc(
        Class<T> hasMementoType) {

        var mementoType = Operators.suppressExceptions(
            () -> hasMementoType.getMethod("getMemento").getReturnType()
        );

        var createMemento = Operators.suppressExceptions(() ->
                hasMementoType.getMethod("createFromMemento", PlatformContext.class, mementoType),
            MessageFormat.format(
                "HasMemento-type `{0}` does not implement method createFromMemento(PlatformContext ctx, M memento).",
                hasMementoType.getName()
            )
        );

        if (CompletionStage.class.isAssignableFrom(createMemento.getReturnType())) {
            return (ctx, memento) -> (CompletionStage<T>) createMemento.invoke(null, ctx, memento);
        } else if (hasMementoType.isAssignableFrom(createMemento.getReturnType())) {
            return (ctx, memento) -> CompletableFuture.completedFuture((T) createMemento.invoke(null, ctx, memento));
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                "Response type of `{0}#{1}` is not `{0}` or `CompletionStage<{0}>`.",
                hasMementoType.getName(), createMemento.getName()
            ));
        }
    }

}
