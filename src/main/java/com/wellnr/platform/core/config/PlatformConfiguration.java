package com.wellnr.platform.core.config;

import com.wellnr.platform.common.config.Configs;
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
public class PlatformConfiguration {

    @Value("name")
    String name;

    @Value("version")
    String version;

    @Value("environment")
    String environment;

    @Value("description")
    String description;

    /**
     * The name of the resource which includes the banner template shown in startup.
     */
    @Value("banner")
    String banner;

    /**
     * The port where Platform API should listen.
     */
    @Value("port")
    int port;

    /**
     * The hostname where Platform API should listen.
     */
    @Value("host")
    String host;

    /**
     * The HTTP header name which contains the user name, provided by the authentication provider.
     */
    @Value("user-id-header-name")
    String userIdHeaderName;

    /**
     * The HTTP header name which contains the user roles, provided by the authentication provider.
     */
    @Value("user-roles-header-name")
    String userRolesHeaderName;

    /**
     * The HTTP header name which contains the user details.
     */
    @Value("user-details-header-name")
    String userDetailsHeaderName;

    /**
     * The HTTP header name which contains the application ID
     */
    @Value("application-id-header-name")
    String applicationIdHeaderName;

    /**
     * The HTTP header name which contains the application secret
     */
    @Value("application-secret-header-name")
    String applicationSecretHeaderName;

    /**
     * The HTTP header name which may contain the id of the authentication token.
     */
    @Value("auth-token-id-header-name")
    String authTokenIdHeaderName;

    /**
     * The HTTP header name which may contain the secret for the authentication token.
     */
    @Value("auth-token-secret-header-name")
    String authTokenSecretHeaderName;

    public static PlatformConfiguration apply() {
        return Configs.mapToConfigClass(PlatformConfiguration.class, "platform");
    }

}
