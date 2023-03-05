package com.wellnr.platform.core.modules.users.exceptions;

import com.wellnr.platform.common.exceptions.DomainException;
import com.wellnr.platform.common.guid.GUID;

import java.text.MessageFormat;

public final class UserAlreadyRegisteredException extends DomainException {

    private UserAlreadyRegisteredException(String message) {
        super(message);
    }

    public static UserAlreadyRegisteredException byUserId(String id) {
        return new UserAlreadyRegisteredException(MessageFormat.format(
            "A user with the provided user id `{0}` already exists.", id
        ));
    }

}
