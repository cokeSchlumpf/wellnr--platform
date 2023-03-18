package com.wellnr.platform.common.guid;

import com.google.common.collect.Maps;
import com.wellnr.platform.common.tuples.Tuple;
import com.wellnr.platform.common.tuples.Tuple2;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GUIDElement {

    private static final Pattern pattern;

    String name;

    Map<String, String> attributes;

    static {
        var patternRegEx = "^([a-z0-9A-Z-]*)(\\[([a-zA-Z0-9-=,'_]+)\\])?$";
        pattern = Pattern.compile(patternRegEx);
    }

    public static GUIDElement apply(String name, Map<String, String> attributes) {
        /*
         * Validate parameters.
         */
        if (!name.matches("[a-z0-9-]+")) {
            throw new IllegalArgumentException(MessageFormat.format(
                "`{0}` is not a valid value for `name`. `name` must adhere regular expression `[a-z][a-z0-9-]*`",
                name
            ));
        }

        attributes.forEach((key, value) -> {
            if (!key.matches("[a-z][a-z0-9-_]*")) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "`{0}` is no valid attribute name, attribute names must adhere regular expression " +
                        "`[a-z][a-z0-9-_]*`",
                    key
                ));
            }

            if (!value.matches("[a-zA-Z0-9-_]*")) {
                throw new IllegalArgumentException(MessageFormat.format(
                    "`{0}` is no valid attribute value, attribute values must adhere regular expression " +
                        "`[a-zA-Z0-9-_]*`",
                    key
                ));
            }
        });

        return new GUIDElement(name, attributes);
    }

    public static GUIDElement apply(String name) {
        return apply(name, Map.of());
    }

    @SafeVarargs
    public static GUIDElement apply(String name, Tuple2<String, Object>... attributes) {
        var map = Arrays
            .stream(attributes)
            .map(t -> Tuple.apply(t._1, t._2.toString()))
            .collect(Collectors.toMap(Tuple2::get_1, Tuple2::get_2));

        return apply(name, map);
    }

    public static GUIDElement fromString(String element) {
        var matcher = pattern.matcher(element);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(MessageFormat.format(
                "`{0}` is no valid GUID element.",
                element
            ));
        }

        var name = matcher.group(1);
        var attributes = matcher.groupCount() > 2 ? matcher.group(3) : "";
        var attributesMap = Maps.<String, String>newHashMap();

        if (Objects.nonNull(attributes) && attributes.length() > 0) {
            var pairs = attributes.split(",");
            for (String pair : pairs) {
                var keyValue = pair.replace("'", "").split("=");
                attributesMap.put(keyValue[0], keyValue[1]);
            }
        }

        return apply(name, attributesMap);
    }

    /**
     * Returns an attribute value.
     *
     * @param name The name of the attribute.
     * @return The value of the attribute.
     * @throws IllegalArgumentException if attribute is not found.
     */
    public String getAttribute(String name) {
        if (this.attributes.containsKey(name)) {
            return this.attributes.get(name);
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                "`{0}` does not exist within this path element (`{0}`)",
                name, this.toString()
            ));
        }
    }

    public String toString() {
        var properties =
            this
                .attributes
                .entrySet()
                .stream()
                .map(entry -> MessageFormat.format("{0}=''{1}''", entry.getKey(), entry.getValue()))
                .sorted()
                .collect(Collectors.joining(","));

        if (properties.length() > 0) {
            properties = MessageFormat.format("[{0}]", properties);
        }

        return MessageFormat.format("{0}{1}", name, properties);
    }

}
