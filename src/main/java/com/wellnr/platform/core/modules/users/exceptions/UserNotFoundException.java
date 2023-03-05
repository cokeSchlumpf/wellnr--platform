package com.wellnr.platform.core.modules.users.exceptions;

import com.wellnr.platform.common.exceptions.DomainException;
import com.wellnr.platform.common.guid.GUID;

import java.text.MessageFormat;

public final class UserNotFoundException extends DomainException {

    private UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException byGUID(GUID id) {
        return new UserNotFoundException(MessageFormat.format(
            "No user found with GUID `{0}`", id
        ));
    }

}
