package com.wellnr.platform.core.services;

import com.wellnr.platform.common.exceptions.NotAuthorizedException;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.context.PlatformContext;
import com.wellnr.platform.core.modules.users.values.rbac.Role;
import com.wellnr.platform.core.modules.users.values.rbac.RoleAssignment;
import com.wellnr.platform.core.modules.users.values.users.AnonymousUser;
import com.wellnr.platform.core.modules.users.values.users.RegisteredUser;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import samples.services.CarPermissions;
import samples.services.CarServices;
import samples.services.CarServicesImpl;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceImplementationProxyTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        /*
         * Setup context.
         */
        var role = Role.apply("/app/cars/roles/admin", CarPermissions.MANAGE_CAR);

        var registeredUserGUID = GUID.apply("/app/users/user/abc");

        var roleAssignment = RoleAssignment.apply(
            registeredUserGUID, GUID.apply("/app/cars"), role
        );

        var registeredUser = RegisteredUser.apply("alex", "Alex", List.of(roleAssignment));
        var anonymousUSer = AnonymousUser.apply();

        var context = PlatformContext.apply();
        context.initialize();

        /*
         * Create service instance.
         */
        var service = ServiceImplementationProxy.createService(
            context, CarServices.class, CarServicesImpl.apply()
        );

        /*
         * Call generated methods with registered user.
         */
        service.createCar(registeredUser, "BMW", "my-car").toCompletableFuture().get();
        service.inspect(registeredUser, "my-car", "lorem ipsum dolor").toCompletableFuture().get();

        var result = service.getCurrentLocation(registeredUser, "my-car").toCompletableFuture().get();
        assertEquals("Merzig", result);

        /*
         * Call generated methods with anonymous user.
         */
        var ex = assertThrows(
            Exception.class,
            () -> service.getCurrentLocation(anonymousUSer, "my-car").toCompletableFuture().get()
        );

        assertTrue(ExceptionUtils.getRootCause(ex) instanceof NotAuthorizedException);

        /*
         * Call custom implementation method.
         */
        assertEquals("42", service.someComplexMethod(registeredUser, 42).toCompletableFuture().get());
    }

}
