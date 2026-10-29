package org.eclipse.swt.layout;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** {@code toString()} must describe the object, not throw, while its impl is unset. */
public class RowLayoutToStringWithoutImplTest {

    @Test
    void toString_without_impl_describes_the_object() {
        RowLayout layout = RowLayout.createApi((IRowLayout) null);
        assertThat(layout.toString()).startsWith("RowLayout {");
    }
}
