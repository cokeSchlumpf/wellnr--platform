package com.wellnr.platform.core.modules.users;

import com.wellnr.platform.core.modules.PlatformModule;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "apply")
public class UsersModule implements PlatformModule {

    public static String NAME = "users";

    public static String GUID_PREFIX = "/app/modules/users";

    @Override
    public String getName() {
        return NAME;
    }

}
