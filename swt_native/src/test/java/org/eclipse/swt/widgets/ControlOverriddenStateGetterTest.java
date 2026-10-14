package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Upstream {@code isEnabled()} is {@code getEnabled() && parent.isEnabled()}, and {@code isVisible()}
 * likewise reads {@code getVisible()}. A self-painted control that keeps its own enabled/visible flag
 * and overrides the getter must be seen through both predicates.
 */
@ExtendWith(Mocks.class)
class ControlOverriddenStateGetterTest {

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
    @DisplayName("isEnabled() honours a subclass that overrides getEnabled()")
    void isEnabledUsesOverriddenGetEnabled() {
        SelfStateCanvas canvas = new SelfStateCanvas(Mocks.shell());
        canvas.setEnabled(false);

        assertThat(canvas.getEnabled()).isFalse();
        assertThat(canvas.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("isVisible() honours a subclass that overrides getVisible()")
    void isVisibleUsesOverriddenGetVisible() {
        Shell shell = Mocks.shell();
        when(shell.isVisible()).thenReturn(true);
        SelfStateCanvas canvas = new SelfStateCanvas(shell);
        assertThat(canvas.isVisible()).isTrue();

        canvas.hidden = true;

        assertThat(canvas.isVisible()).isFalse();
    }

    private static class SelfStateCanvas extends Canvas {
        private boolean enabled = true;
        private boolean hidden;

        SelfStateCanvas(Composite parent) {
            super(parent, SWT.DOUBLE_BUFFERED);
        }

        @Override
        public void setEnabled(boolean enabled) {
            checkWidget();
            this.enabled = enabled;
            redraw();
        }

        @Override
        public boolean getEnabled() {
            return enabled;
        }

        @Override
        public boolean getVisible() {
            return !hidden && super.getVisible();
        }

        @Override
        protected void checkSubclass() {
        }
    }
}
