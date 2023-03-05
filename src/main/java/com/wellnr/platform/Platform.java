package com.wellnr.platform;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor(staticName = "apply")
public class Platform {

    private static final Logger LOG = LoggerFactory.getLogger(Platform.class);

    private final PlatformRuntime runtime;

    /**
     * Creates a new Maquette instance.
     *
     * @return The new instance.
     */
    public static Platform apply() {
        var runtime = PlatformRuntime.apply();
        return Platform.apply(runtime);
    }

    public void start() {
        LOG.info("Starting application.");
    }

}
