package com.wellnr.platform.common.config;

import com.wellnr.platform.common.config.annotations.ConfigurationProperties;
import com.wellnr.platform.common.config.annotations.Value;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@ConfigurationProperties
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(staticName = "apply")
public class MaquetteConfiguration {

    @Value("name")
    String name;

    @Value("version")
    String version;

    @Value("environment")
    String environment;

    @Value("core")
    CoreConfiguration core;

    public static MaquetteConfiguration apply() {
        return Configs.mapToConfigClass(MaquetteConfiguration.class, "maquette");
    }

}
