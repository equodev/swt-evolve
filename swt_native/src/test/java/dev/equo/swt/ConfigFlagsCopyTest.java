package dev.equo.swt;

import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigFlagsCopyTest {

    @Test
    void aCopyHoldsEveryFlagOfTheOriginal() throws Exception {
        ConfigFlags original = new ConfigFlags();
        List<Field> fields = instanceFields();
        for (Field f : fields) f.set(original, notDefault(f.getType()));

        ConfigFlags copy = original.copy();

        assertThat(copy).isNotSameAs(original);
        for (Field f : fields) {
            if (f.getType().isPrimitive()) assertThat(f.get(copy)).as(f.getName()).isEqualTo(f.get(original));
            else assertThat(f.get(copy)).as(f.getName() + ", shared as a shallow copy shares it").isSameAs(f.get(original));
        }
    }

    @Test
    void changingTheCopyLeavesTheOriginalAlone() {
        ConfigFlags original = new ConfigFlags();
        original.force_theme = "light";

        ConfigFlags copy = original.copy();
        copy.force_theme = "dark";

        assertThat(original.force_theme).isEqualTo("light");
    }

    private static List<Field> instanceFields() {
        List<Field> fields = new ArrayList<>();
        for (Field f : ConfigFlags.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) continue;
            f.setAccessible(true);
            fields.add(f);
        }
        assertThat(fields).isNotEmpty();
        return fields;
    }

    /** A value of {@code type} that a fresh ConfigFlags does not hold. */
    private static Object notDefault(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) return true;
        if (type == int.class) return 7;
        if (type == double.class) return 1.5;
        if (type == String.class) return new String("set");
        if (type == java.util.Map.class) return new HashMap<>(java.util.Map.of("k", new RGB(1, 2, 3)));
        if (type.isEnum()) return type.getEnumConstants()[type.getEnumConstants().length - 1];
        throw new AssertionError("no test value for a flag of type " + type + ": add one");
    }
}
