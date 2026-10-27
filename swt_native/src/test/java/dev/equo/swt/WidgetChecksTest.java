package dev.equo.swt;

import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Two checks every widget makes as SWT's do: an application subclass is refused unless its own
 * checkSubclass() allows it, and the style and data accessors refuse a disposed widget. The subclasses
 * live outside org.eclipse.swt.widgets, where any class counts as one of SWT's own.
 */
@ExtendWith(Mocks.class)
class WidgetChecksTest {

    static class AllowedLabel extends Label {
        AllowedLabel(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected void checkSubclass() {
        }
    }

    static class PlainLabel extends Label {
        PlainLabel(Composite parent) {
            super(parent, SWT.NONE);
        }
    }

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("an application subclass that allows itself in checkSubclass is built")
    void aSubclassThatAllowsItselfIsBuilt() {
        Shell shell = Mocks.shell();
        assertThatCode(() -> new AllowedLabel(shell)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("an application subclass that does not is refused")
    void aSubclassThatDoesNotIsRefused() {
        Shell shell = Mocks.shell();
        assertThatThrownBy(() -> new PlainLabel(shell))
                .isInstanceOfSatisfying(SWTException.class, e -> org.assertj.core.api.Assertions.assertThat(e.code)
                        .isEqualTo(SWT.ERROR_INVALID_SUBCLASS));
    }

    @Test
    @DisplayName("the style and data accessors refuse a disposed widget")
    void styleAndDataRefuseADisposedWidget() {
        Label label = new Label(Mocks.shell(), SWT.NONE);
        label.dispose();
        for (Runnable call : new Runnable[] { label::getStyle, label::getData, () -> label.getData("k"),
                () -> label.setData("v"), () -> label.setData("k", "v") }) {
            assertThatThrownBy(call::run)
                    .isInstanceOfSatisfying(SWTException.class, e -> org.assertj.core.api.Assertions.assertThat(e.code)
                            .isEqualTo(SWT.ERROR_WIDGET_DISPOSED));
        }
    }
}
