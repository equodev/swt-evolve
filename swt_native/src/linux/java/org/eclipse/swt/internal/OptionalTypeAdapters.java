package org.eclipse.swt.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class OptionalTypeAdapters {

    public static void register(GsonBuilder gson) {
        gson
                .registerTypeAdapterFactory(OPTIONAL_DOUBLE_FACTORY)
                .registerTypeAdapterFactory(OPTIONAL_INT_FACTORY)
                .registerTypeAdapterFactory(OPTIONAL_LONG_FACTORY)
                .registerTypeAdapterFactory(OPTIONAL_FACTORY);
    }

    /**
     * Typeadpater used by  OPTIONAL_FACTORY
     *
     * @author N.Wood
     */
    private static final class OptionalTypeAdapter<T> extends TypeAdapter<Optional<T>> {
        private final TypeAdapter<T> adapter;

        public OptionalTypeAdapter(final TypeAdapter<T> adapter) {
            this.adapter = adapter;
        }

        @Override
        public Optional<T> read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return Optional.empty();
            }
            return Optional.ofNullable(adapter.read(in));
        }

        @Override
        public void write(JsonWriter out, Optional<T> value) throws IOException {
            if (value != null && value.isPresent()) {
                adapter.write(out, value.get());
            } else {
                out.nullValue();
            }
        }
    }

    /**
     * Returns a factory for  {@code Optional}.
     * The inner type will be resolved from the type token
     *
     * @author N.Wood
     */
    public static final TypeAdapterFactory OPTIONAL_FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Class<? super T> rawType = typeToken.getRawType();
            if (!Optional.class.isAssignableFrom(rawType)) {
                return null;
            }

            final Type actualType = containerTypeOf(typeToken.getType());
            final TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(actualType));
            return new OptionalTypeAdapter(adapter);
        }

        private Type containerTypeOf(Type type) {
            if (type instanceof WildcardType) {
                return ((WildcardType) type).getUpperBounds()[0];
            }
            if (type instanceof ParameterizedType) {
                Type actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
                if (actualTypeArgument instanceof Class) {
                    if (Serializable.class.isAssignableFrom(((Class<?>) actualTypeArgument)))
                        return actualTypeArgument;
                }
            }
            return Object.class;
        }
    };

    private static final class OptionalIntTypeAdapter extends TypeAdapter<OptionalInt> {
        private final TypeAdapter<Integer> adapter;

        public OptionalIntTypeAdapter(final TypeAdapter<Integer> adapter) {
            this.adapter = adapter;
        }

        @Override
        public OptionalInt read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return OptionalInt.empty();
            }
            return OptionalInt.of(adapter.read(in));
        }

        @Override
        public void write(JsonWriter out, OptionalInt value) throws IOException {
            if (value != null && value.isPresent()) {
                adapter.write(out, value.getAsInt());
            } else {
                out.nullValue();
            }
        }
    }

    /**
     * Returns a factory for  {@code int}.
     * The inner type will be resolved from the typeToken
     *
     * @author N.Wood
     */

    public static final TypeAdapterFactory OPTIONAL_INT_FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings({"unchecked"})
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Class<? super T> rawType = typeToken.getRawType();
            if (!OptionalInt.class.isAssignableFrom(rawType)) {
                return null;
            }

            final TypeAdapter<Integer> adapter = gson.getAdapter(Integer.class);
            return (TypeAdapter<T>) (new OptionalIntTypeAdapter(adapter));
        }
    };

    private static final class OptionalLongTypeAdapter extends TypeAdapter<OptionalLong> {
        private final TypeAdapter<Long> adapter;

        public OptionalLongTypeAdapter(final TypeAdapter<Long> adapter) {
            this.adapter = adapter;
        }

        @Override
        public OptionalLong read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return OptionalLong.empty();
            }
            return OptionalLong.of(adapter.read(in));
        }

        @Override
        public void write(JsonWriter out, OptionalLong value) throws IOException {
            if (value != null && value.isPresent()) {
                adapter.write(out, value.getAsLong());
            } else {
                out.nullValue();
            }
        }
    }

    /**
     * Returns a factory for  {@code OPTIONAL_LONG_FACTORY}.
     * The inner type will be resolved from the type token
     *
     * @author N.Wood
     */

    public static final TypeAdapterFactory OPTIONAL_LONG_FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings({"unchecked"})
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Class<? super T> rawType = typeToken.getRawType();
            if (!OptionalLong.class.isAssignableFrom(rawType)) {
                return null;
            }

            final TypeAdapter<Long> adapter = gson.getAdapter(Long.class);
            return (TypeAdapter<T>) (new OptionalLongTypeAdapter(adapter));
        }
    };

    private static final class OptionalDoubleTypeAdapter extends TypeAdapter<OptionalDouble> {
        private final TypeAdapter<Double> adapter;

        public OptionalDoubleTypeAdapter(final TypeAdapter<Double> adapter) {
            this.adapter = adapter;
        }

        @Override
        public OptionalDouble read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return OptionalDouble.empty();
            }
            return OptionalDouble.of(adapter.read(in));
        }

        @Override
        public void write(JsonWriter out, OptionalDouble value) throws IOException {
            if (value != null && value.isPresent()) {
                adapter.write(out, value.getAsDouble());
            } else {
                out.nullValue();
            }
        }
    }

    /**
     * Returns a factory for  {@code OPTIONAL_DOUBLE_FACTORY}.
     * The inner type will be resolved from the type token
     *
     * @author N.Wood
     */
    public static final TypeAdapterFactory OPTIONAL_DOUBLE_FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings({"unchecked"})
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Class<? super T> rawType = typeToken.getRawType();
            if (!OptionalDouble.class.isAssignableFrom(rawType)) {
                return null;
            }

            final TypeAdapter<Double> adapter = gson.getAdapter(Double.class);
            return (TypeAdapter<T>) (new OptionalDoubleTypeAdapter(adapter));
        }
    };
}