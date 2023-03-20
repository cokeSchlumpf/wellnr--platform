package com.wellnr.platform.core.modules.users.services;

import com.wellnr.platform.common.tuples.Done;
import com.wellnr.platform.common.validation.ParameterName;
import com.wellnr.platform.core.modules.users.Permissions;
import com.wellnr.platform.core.modules.users.entities.RegisteredUserRootEntity;
import com.wellnr.platform.core.modules.users.entities.RegisteredUsersRootEntity;
import com.wellnr.platform.core.modules.users.values.users.User;
import com.wellnr.platform.core.services.GeneratedCommand;
import com.wellnr.platform.core.services.GeneratedImpl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.concurrent.CompletionStage;

import static com.wellnr.platform.common.Operators.pass;

public interface UserServices {

    // Must be implemented manually because requires more complex logic.
    @GeneratedCommand("complex")
    CompletionStage<Done> doSomethingMoreComplex(
        User executor,
        @NotBlank /* Will validate user input. */ String someParam);

    @GeneratedCommand("register") // Will generate RegisterUserCommand.
    @GeneratedImpl(delegate = RegisteredUsersRootEntity.class) // Will delegate the call to `registerUser` method
    default CompletionStage<Done> registerUser(
        User user,
        @ParameterName("userId") @NotBlank /* Will validate user input. */ String userId,
        @ParameterName("displayName") @NotBlank /* Will validate user input */ String displayName) {

        return pass(); // Use default implementation to avoid mentions in classes which implement complex functions.
    }

    @GeneratedImpl(
        delegate = RegisteredUserRootEntity.class, // Will delegate call to `delete` method (could also be named `deleteRegisteredUser`).
        lookup = RegisteredUsersRootEntity.class, // Will use this class to find specific instance based on available Parameters.
        permissions = {
            Permissions.MANAGE_USERS, // Check if user has permission to manage users on Module level (across all users).
            Permissions.MANAGE_USER // Check if user has permissions on specific/ selected user instance.
        }
    )
    @GeneratedCommand("delete") // Will generate DeleteRegisteredUserCommand.
    default CompletionStage<Done> deleteRegisteredUser(
        @ParameterName("executor") @NotNull /* Will validate user input. */  User executor,
        @ParameterName("externalUserId") @NotNull @NotBlank /* Will validate user input */ String externalUserId) {

        return pass(); // Use default implementation to avoid mentions in classes which implement complex functions.
    }

}
