package com.wellnr.platform.common.guid;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Global Unique ID is used to create unique, immutable, hierarchical identifiers for resources and entities
 * within the application.
 * <p>
 * A GUID consists of a list of hierarchical GUID Elements. Each GUID element may contain attributes.
 * <p>
 * A GUID can be represented as String, elements are separated by `/`. Element names, as well as attribute names
 * must adhere to the regular expression `[a-z][a-z0-9-]*`. Attribute values must adhere to the regular expression
 * `[a-zA-Z0-9-]*`. A GUID always starts with a slash to indicate that this is the root position.
 * <p>
 * Attributes which are used within a GUID must be immutable!
 * <p>
 * Examples
 * --------
 * <p>
 * GUID for a specific user:
 * <p>
 * ```
 * /modules/users/user[id='abc']
 * ```
 * <p>
 * GUID of nested entities:
 * <p>
 * ```
 * /modules/school-management/course[id='abc',name='foo']/students/student[id='xyz']
 * ```
 */
@Value
@JsonSerialize(using = GUID.Serializer.class)
@JsonDeserialize(using = GUID.Deserializer.class)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GUID {

    List<GUIDElement> elements;

    public static GUID apply(Collection<GUIDElement> elements) {
        return new GUID(List.copyOf(elements));
    }

    public static GUID apply(GUIDElement... elements) {
        return apply(Arrays.stream(elements).toList());
    }

    public static GUID apply(String... elements) {
        return apply(Arrays.stream(elements).map(GUIDElement::fromString).toList());
    }

    public static GUID fromString(String guid) {
        var elements = Arrays
            .stream(guid.split("/"))
            .filter(s -> s.strip().length() > 0)
            .map(GUIDElement::fromString)
            .toList();

        return apply(elements);
    }

    @Override
    public String toString() {
        var elements = this.elements
            .stream()
            .map(GUIDElement::toString)
            .collect(Collectors.joining("/"));

        return MessageFormat.format("/{0}", elements);
    }

    /**
     * Get an attribute from the GUID using a selector. A selector has the following format:
     * <p>
     * ```
     * [${ELEMENT_NAME}#]${ATTRIBUTE}
     * ```
     * <p>
     * For example, the following will extract attribute `id` from an element of name `user`.
     * <p>
     * ```
     * user#id
     * ```
     * <p>
     * The following will extract an attribute `user_id` from the first element where this value is found.
     * <p>
     * ```
     * user_id
     * ```
     * <p>
     * Search always begins at the leaf element of the GUID, going up to the root element.
     *
     * @param selector The selector.
     * @return The value of the attribute.
     * @throws IllegalArgumentException If attribute does not exist in the GUID.
     */
    public String getAttribute(String selector) {
        if (selector.contains("#")) {
            var parts = selector.split("#");
            var elementName = parts[0];
            var attribute = parts[1];

            return Lists.reverse(this.elements)
                .stream()
                .filter(el -> el.getName().equals(elementName))
                .map(el -> el.getAttribute(attribute))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(MessageFormat.format(
                    "Can't select `{0}` from GUID `{1}`", selector, this.toString()
                )));
        } else {
            return Lists.reverse(this.elements)
                .stream()
                .map(el -> el.getAttribute(selector))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(MessageFormat.format(
                    "Can't select `{0}` from GUID `{1}`", selector, this.toString()
                )));
        }
    }

    public static class Serializer extends StdSerializer<GUID> {

        private Serializer() {
            super(GUID.class);
        }

        @Override
        public void serialize(GUID value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }

    }

    public static class Deserializer extends StdDeserializer<GUID> {

        private Deserializer() {
            super(GUID.class);
        }

        @Override
        public GUID deserialize(JsonParser p, DeserializationContext ignore) throws IOException {
            return GUID.fromString(p.readValueAs(String.class));
        }

    }

}
