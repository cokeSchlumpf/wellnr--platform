package com.wellnr.platform.core.commands;


import java.util.Optional;

/**
 * This interface describes abstract results from Commands (HTTP requests to Maquette). Results might be displayed
 * in various formats to support multiple Content-Types.
 */
public sealed interface CommandResult permits DataResult {

    /**
     * Transform/ display the result as text representation (media type text/plain).
     * Fallback is always plain JSON representation.
     *
     * @param runtime The Maquette runtime configuration.
     * @return The result as plain text.
     */
    /*
    default String toPlainText(MaquetteRuntime runtime) {
        return Operators.suppressExceptions(() -> runtime
            .getObjectMapperFactory()
            .createJsonMapper(true)
            .writeValueAsString(this));
    }
     */

    /**
     * Some results might be represented as tabular data (media type text/comma-separated-values).
     *
     * @param runtime The Maquette runtime configuration.
     * @return The result as plain text.
     */
    /*
    default Optional<String> toCSV(MaquetteRuntime runtime) {
        return Optional.empty();
    }
    */

}
