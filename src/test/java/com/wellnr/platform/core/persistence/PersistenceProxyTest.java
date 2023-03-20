package com.wellnr.platform.core.persistence;

import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.modules.users.values.users.AnonymousUser;
import com.wellnr.platform.core.services.ServiceImplementationProxy;
import org.junit.jupiter.api.Test;
import samples.services.CarServices;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersistenceProxyTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        var context = PlatformContext.apply();
        var user = AnonymousUser.apply(List.of());

        context.initialize();

        var service = ServiceImplementationProxy.createService(
            context, CarServices.class, null
        );

        service.createCar(user, "BMW", "my-car").toCompletableFuture().get();
        service.inspect(user, "my-car", "lorem ipsum dolor").toCompletableFuture().get();

        var result = service.getCurrentLocation(user, "my-car").toCompletableFuture().get();
        assertEquals("Merzig", result);
    }

}
