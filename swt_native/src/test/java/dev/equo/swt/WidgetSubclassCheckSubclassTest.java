package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartButton;
import org.eclipse.swt.widgets.DartText;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * An application subclass of a widget allows itself by overriding {@code checkSubclass()} with an
 * empty body, the idiom SWT documents. The Dart backend must consult that override when the widget
 * is constructed, and keep rejecting a subclass that does not override it.
 *
 * <p>The subclasses live outside {@code org.eclipse.swt.widgets} on purpose: that package prefix is
 * what the subclass check accepts, so a nested class in the widgets package would pass regardless.
 */
class WidgetSubclassCheckSubclassTest extends SerializeTestBase {

    static class AllowedText extends Text {
        AllowedText(Composite parent, int style) {
            super(parent, style);
        }

        @Override
        protected void checkSubclass() {
        }
    }

    static class UncheckedText extends Text {
        UncheckedText(Composite parent, int style) {
            super(parent, style);
        }
    }

    static class AllowedButton extends Button {
        AllowedButton(Composite parent, int style) {
            super(parent, style);
        }

        @Override
        protected void checkSubclass() {
        }
    }

    static class UncheckedButton extends Button {
        UncheckedButton(Composite parent, int style) {
            super(parent, style);
        }
    }

    @Test
    @DisplayName("a Text subclass that overrides checkSubclass() is constructed")
    void textSubclassWithOverrideIsConstructed() {
        Shell shell = Mocks.shell();

        AllowedText text = new AllowedText(shell, SWT.SINGLE);

        assertThat(text.getImpl()).isInstanceOf(DartText.class);
        assertThat(text.isDisposed()).isFalse();
    }

    @Test
    @DisplayName("a Text subclass without the override is still rejected")
    void textSubclassWithoutOverrideIsRejected() {
        Shell shell = Mocks.shell();

        assertThatThrownBy(() -> new UncheckedText(shell, SWT.SINGLE))
                .isInstanceOf(SWTException.class)
                .hasMessage("Subclassing not allowed");
    }

    @Test
    @DisplayName("a Button subclass that overrides checkSubclass() is constructed")
    void buttonSubclassWithOverrideIsConstructed() {
        Shell shell = Mocks.shell();

        AllowedButton button = new AllowedButton(shell, SWT.PUSH);

        assertThat(button.getImpl()).isInstanceOf(DartButton.class);
        assertThat(button.isDisposed()).isFalse();
    }

    @Test
    @DisplayName("a Button subclass without the override is still rejected")
    void buttonSubclassWithoutOverrideIsRejected() {
        Shell shell = Mocks.shell();

        assertThatThrownBy(() -> new UncheckedButton(shell, SWT.PUSH))
                .isInstanceOf(SWTException.class)
                .hasMessage("Subclassing not allowed");
    }
}
