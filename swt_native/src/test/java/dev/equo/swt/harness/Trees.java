package dev.equo.swt.harness;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tagged values ({@link dev.equo.swt.comm.TaggedValues}) as the Java objects a test tool reads them
 * into: maps onto public fields by name, lists onto arrays, numbers onto whatever number a field is.
 */
public final class Trees {

    private Trees() {
    }

    /** {@code bytes}, tagged values, as {@code type}; null when they are not tagged values. */
    public static <T> T read(Class<T> type, byte[] bytes) {
        if (bytes == null || !dev.equo.swt.comm.TaggedValues.isTagged(bytes, 0, bytes.length)) return null;
        return type.cast(as(type, type, dev.equo.swt.comm.TaggedValues.decode(bytes, 0, bytes.length)));
    }

    private static Object as(Class<?> type, Type generic, Object tree) {
        if (tree == null) return type.isPrimitive() ? Array.get(Array.newInstance(type, 1), 0) : null;
        if (type == Object.class) return tree;
        if (tree instanceof Number n) {
            if (type == int.class || type == Integer.class) return n.intValue();
            if (type == long.class || type == Long.class) return n.longValue();
            if (type == double.class || type == Double.class) return n.doubleValue();
            if (type == float.class || type == Float.class) return n.floatValue();
            if (type == short.class || type == Short.class) return n.shortValue();
            if (type == byte.class || type == Byte.class) return n.byteValue();
        }
        if (type.isArray() && tree instanceof List<?> list) {
            Object out = Array.newInstance(type.getComponentType(), list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(out, i, as(type.getComponentType(), type.getComponentType(), list.get(i)));
            }
            return out;
        }
        if (Map.class.isAssignableFrom(type) && tree instanceof Map<?, ?> map) {
            Class<?> valueType = Object.class;
            if (generic instanceof ParameterizedType p && p.getActualTypeArguments()[1] instanceof Class<?> c) valueType = c;
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : map.entrySet()) out.put(String.valueOf(e.getKey()), as(valueType, valueType, e.getValue()));
            return out;
        }
        if (tree instanceof Map<?, ?> map) return object(type, map);
        return type.isInstance(tree) ? tree : null;
    }

    private static Object object(Class<?> type, Map<?, ?> map) {
        try {
            if (type.isRecord()) {
                var components = type.getRecordComponents();
                Object[] args = new Object[components.length];
                Class<?>[] types = new Class<?>[components.length];
                for (int i = 0; i < components.length; i++) {
                    types[i] = components[i].getType();
                    args[i] = as(types[i], components[i].getGenericType(), map.get(components[i].getName()));
                }
                var constructor = type.getDeclaredConstructor(types);
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            }
            Object out = instance(type);
            for (Field f : type.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || !map.containsKey(f.getName())) continue;
                Object value = as(f.getType(), f.getGenericType(), map.get(f.getName()));
                if (value == null && f.getType().isPrimitive()) continue;
                f.setAccessible(true);
                f.set(out, value);
            }
            return out;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /** A {@code type} to fill in: from its no-argument constructor, or any other given zeros and nulls. */
    private static Object instance(Class<?> type) throws ReflectiveOperationException {
        java.lang.reflect.Constructor<?> chosen = null;
        for (java.lang.reflect.Constructor<?> c : type.getDeclaredConstructors()) {
            if (chosen == null || c.getParameterCount() < chosen.getParameterCount()) chosen = c;
        }
        if (chosen == null) throw new NoSuchMethodException(type.getName());
        Object[] args = new Object[chosen.getParameterCount()];
        Class<?>[] types = chosen.getParameterTypes();
        for (int i = 0; i < args.length; i++) {
            args[i] = types[i].isPrimitive() ? Array.get(Array.newInstance(types[i], 1), 0) : null;
        }
        chosen.setAccessible(true);
        return chosen.newInstance(args);
    }
}
