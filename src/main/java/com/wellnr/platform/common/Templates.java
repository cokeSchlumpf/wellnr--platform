package com.wellnr.platform.common;

import com.google.common.collect.Maps;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.StringLoader;

import java.io.StringWriter;
import java.util.Map;

/**
 * Util class to work with templates.
 *
 * @author Michael Wellner (michaelwellner@kpmg.com).
 */
public final class Templates {

    /**
     * Do not create instances.
     */
    private Templates() {

    }

    /**
     * Renders a template from a classpath resource with Jtwig.
     *
     * @param resourcePath The path of the resource
     * @return The rendered resource
     */
    public static String renderTemplateFromResources(String resourcePath) {
        return renderTemplateFromResources(resourcePath, Maps.newHashMap());
    }

    /**
     * Renders a template from a classpath resource with Jtwig using a model created from a map.
     *
     * @param resourcePath The path of the resource
     * @param values       A map of values to be injected into the model
     * @return The rendered resource
     */
    public static String renderTemplateFromResources(String resourcePath, Map<String, Object> values) {
        return Operators.suppressExceptions(() -> {
            var engine = new PebbleEngine.Builder()
                .autoEscaping(false)
                .build();
            var template = engine.getTemplate(resourcePath);
            var writer = new StringWriter();
            template.evaluate(writer, values);

            return writer.toString();
        });
    }

    /**
     * Renders a template passed as string.
     *
     * @param template The template to be rendered.
     * @param values The values which can be used in the template.
     * @return The rendered template.
     */
    public static String renderTemplateFromString(String template, Map<String, Object> values) {
        var engine = new PebbleEngine.Builder()
            .autoEscaping(false)
            .loader(new StringLoader())
            .build();

        var compiledTemplate = engine.getTemplate(template);
        var writer = new StringWriter();
        Operators.suppressExceptions(() -> compiledTemplate.evaluate(writer, values));
        return writer.toString();
    }

}
