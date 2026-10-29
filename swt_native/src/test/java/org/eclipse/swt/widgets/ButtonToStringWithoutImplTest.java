package org.eclipse.swt.widgets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** {@code toString()} must describe the object, not throw, while its impl is unset. */
public class ButtonToStringWithoutImplTest {

    @Test
    void toString_without_impl_describes_the_object() {
        Button button = Button.createApi((IButton) null);
        assertThat(button.toString()).startsWith("Button {");
    }

    @Test
    void toString_without_impl_names_an_anonymous_subclass() {
        Button button = new Button((IButton) null) {};
        assertThat(button.toString()).startsWith("ButtonToStringWithoutImplTest$1 {");
    }
}
