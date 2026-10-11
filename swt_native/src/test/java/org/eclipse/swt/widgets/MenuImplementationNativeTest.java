package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What {@link DartMenu} does when it is driven directly rather than through {@link Menu}. */
@Tag("native-unit")
class MenuImplementationNativeTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a menu bar is visible exactly while it is its shell's menu bar")
    void menuBarIsVisibleWhileItIsTheShellsMenuBar() {
        Menu bar = new Menu(shell, SWT.BAR);
        Menu other = new Menu(shell, SWT.BAR);

        assertThat(bar.getVisible()).isFalse();

        ((DartDecorations) shell.getImpl()).menuBar = bar;

        assertThat(bar.getVisible()).isTrue();
        assertThat(bar.isVisible()).isTrue();
        assertThat(other.getVisible()).isFalse();

        ((DartDecorations) shell.getImpl()).menuBar = other;

        assertThat(bar.getVisible()).isFalse();
        assertThat(other.getVisible()).isTrue();
    }

    @Test
    @DisplayName("a menu registers with its display, which is how disposing the shell finds it")
    void menuRegistersWithItsDisplay() {
        Menu popUp = new Menu(shell, SWT.POP_UP);
        Menu dropDown = new Menu(shell, SWT.DROP_DOWN);

        DartDisplay display = (DartDisplay) shell.getDisplay().getImpl();
        verify(display).addMenu(popUp);
        verify(display).addMenu(dropDown);
    }
}
