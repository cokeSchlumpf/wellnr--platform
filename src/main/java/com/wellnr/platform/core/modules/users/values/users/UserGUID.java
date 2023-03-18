package com.wellnr.platform.core.modules.users.values.users;

import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.guid.GUID;
import com.wellnr.platform.core.modules.users.UsersModule;

import java.text.MessageFormat;

public final class UserGUID {

    private UserGUID() {

    }

    public static GUID apply() {
        return apply(Operators.randomHash());
    }

    public static GUID apply(String userId) {
        return GUID.fromString(MessageFormat.format(
            "{0}/user[user_id=''{1}'']",
            UsersModule.GUID_PREFIX, userId
        ));
    }

}
