package com.wellnr.platform.core.commands;


import com.wellnr.platform.common.Operators;
import com.wellnr.platform.common.databind.ObjectMapperFactory;
import com.wellnr.platform.core.context.PlatformContext;

/**
 * This interface describes abstract results from Commands (HTTP requests to Platform). Results might be displayed
 * in various formats to support multiple Content-Types.
 */
public sealed interface CommandResult permits DataResult {

    /**
     * Transform/ display the result as text representation (media type text/plain).
     * Fallback is always plain JSON representation.
     *
     * @param context The Platform runtime configuration.
     * @return The result as plain text.
     */
    default String toPlainText(PlatformContext context) {
        return Operators.suppressExceptions(() -> context
            .getInstance(ObjectMapperFactory.class)
            .createJsonMapper(true)
            .writeValueAsString(this));
    }

    /**
     * Some results might be represented as tabular data (media type text/comma-separated-values).
     *
     * @param runtime The Platform runtime configuration.
     * @return The result as plain text.
     */
    /*
    default Optional<String> toCSV(PlatformRuntime runtime) {
        return Optional.empty();
    }
    */

}
