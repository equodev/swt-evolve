package org.eclipse.swt.graphics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** {@code toString()} must describe the object, not throw, while its impl is unset. */
public class ColorToStringWithoutImplTest {

    @Test
    void toString_without_impl_describes_the_object() {
        Color color = Color.createApi((IColor) null);
        assertThat(color.toString()).startsWith("Color {");
    }
}
