package com.wellnr.platform;

import io.javalin.Javalin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

@Getter
@AllArgsConstructor(staticName = "apply")
public class PlatformRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformRuntime.class);

    /**
     * The underlying webserver. This will be set and initialized during initialization.
     */
    @Nullable
    Javalin app;

    public static PlatformRuntime apply() {
        return apply(null);
    }

}
