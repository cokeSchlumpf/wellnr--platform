package com.wellnr.platform.core.modules.users.configuration;

import com.wellnr.platform.common.config.Configs;
import com.wellnr.platform.common.config.annotations.ConfigurationProperties;
import com.wellnr.platform.common.config.annotations.Value;
import com.wellnr.platform.core.config.MongoDatabaseConfiguration;
import com.wellnr.platform.core.config.RepositoryMode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@ConfigurationProperties
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(staticName = "apply")
public class UsersConfiguration {

    /**
     * Specify which repository implementation should be used.
     */
    @Value("mode")
    RepositoryMode mode;

    @Value("database")
    MongoDatabaseConfiguration database;

    public static UsersConfiguration apply() {
        return Configs.mapToConfigClass(UsersConfiguration.class, "platform.users");
    }

}
