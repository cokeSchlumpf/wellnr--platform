package com.wellnr.platform.core.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.wellnr.platform.common.config.annotations.ConfigurationProperties;
import com.wellnr.platform.common.config.annotations.Optional;
import com.wellnr.platform.common.config.annotations.Value;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Configure a connection to a Mongo Database.
 *
 * Either <code>connection-properties</code> or <code>connection-string</code> must be set.
 */
@Getter
@ConfigurationProperties
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor(staticName = "apply")
public class MongoDatabaseConfiguration {

    /**
     * The name of the database to be used.
     */
    @Value("databaseName")
    String databaseName;

    /**
     * Connection properties to connect to the database host.
     */
    @Optional
    @Value("connection-properties")
    ConnectionProperties connectionProperties;

    /**
     * Connection string to connect to the database host.
     */
    @Optional
    @Value("connection-string")
    String connectionString;

    /**
     * Creates a new instance from connection properties.
     *
     * @param databaseName See {@link MongoDatabaseConfiguration#databaseName}.
     * @param connectionProperties See {@link MongoDatabaseConfiguration#connectionProperties}.
     * @return A new instance.
     */
    public static MongoDatabaseConfiguration apply(String databaseName, ConnectionProperties connectionProperties) {
        return apply(databaseName, connectionProperties, null);
    }

    /**
     * Creates a new instance form connection string.
     *
     * @param databaseName See {@link MongoDatabaseConfiguration#databaseName}.
     * @param connectionString See {@link MongoDatabaseConfiguration#connectionString}.
     * @return A new instance.
     */
    public static MongoDatabaseConfiguration apply(String databaseName, String connectionString) {
        return apply(databaseName, null, connectionString);
    }

    /**
     * See {@link MongoDatabaseConfiguration#connectionString}.
     *
     * @return The connection string, if configured.
     */
    public java.util.Optional<String> getConnectionString() {
        return java.util.Optional.of(this.connectionString);
    }

    /**
     * See @{@link MongoDatabaseConfiguration#connectionProperties}.
     *
     * @return The connection properties, if configured.
     */
    public java.util.Optional<ConnectionProperties> getConnectionProperties() {
        return java.util.Optional.of(this.connectionProperties);
    }

    /**
     * This method will create a Mongo database client based on the configuration.
     *
     * @return A new Mongo Database client.
     */
    public MongoDatabase getClient() {
        var connectionString = this.connectionString;

        if (Objects.isNull(connectionString)) {
            if (Objects.isNull(this.connectionProperties)) {
                throw new IllegalStateException(
                    "Either `connection-string` or `connection-properties` must be set, but both are `null`-"
                );
            }

            connectionString = connectionProperties.getConnectionString();
        }

        var settings = MongoClientSettings
            .builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .build();

        var client = MongoClients.create(settings);
        return client.getDatabase(databaseName);
    }

    /**
     * Single connection properties to build connection string.
     */
    @Getter
    @ConfigurationProperties
    @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
    @AllArgsConstructor(staticName = "apply")
    public static class ConnectionProperties {

        /**
         * The hostname of the database server.
         */
        @Value("host")
        String host;

        /**
         * The port on which the database listens.
         */
        @Value("port")
        String port;

        /**
         * Additional options.
         */
        @Value("options")
        String options;

        /**
         * The username used to authenticate on the database.
         */
        @Value("username")
        String username;

        /**
         * The password used for authentication.
         */
        @Value("password")
        String password;

        /**
         * Returns a connection string based on configuration.
         *
         * @return The connection string.
         */
        public String getConnectionString() {
            return String.format(
                "mongodb://%s:%s@%s:%s/%s",
                getUsername(),
                getPassword(),
                getHost(),
                getPort(),
                getOptions()
            );
        }

    }

}
