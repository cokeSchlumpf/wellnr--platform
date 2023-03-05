package com.wellnr.platform.core.commands;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Value;

import java.io.IOException;

/**
 * Return any kind of structured data, usually some JSON object tree.
 *
 * @param <T> The actual type of the data.
 */
@Value
public class DataResult<T> implements CommandResult {

    T data;

    public static <T> DataResult<T> apply(T data) {
        return new DataResult<>(data);
    }

    @SuppressWarnings("rawtypes")
    public static class Serializer extends StdSerializer<DataResult> {

        @SuppressWarnings("unused")
        protected Serializer() {
            super(DataResult.class);
        }

        @Override
        public void serialize(DataResult value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeObject(value.data);
        }

    }

}
