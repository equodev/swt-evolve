package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** A shell's maximum size. Apart from {@link ShellStateNativeTest} because the API only exists from SWT 3.116. */
@Tag("native-unit")
class ShellMaximumSizeNativeTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        Display display = DartMocks.dartDisplay();
        when(((DartDisplay) display.getImpl()).isValidThread()).thenReturn(true);
        when(display.getShells()).thenReturn(new Shell[0]);
        shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setBounds(10, 20, 250, 180);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a shell with no maximum given has one nothing can reach")
    void defaultMaximum() {
        assertThat(shell.getMaximumSize()).isEqualTo(new Point(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("the maximum is handed out as a copy")
    void maximumIsACopy() {
        shell.setMaximumSize(900, 700);
        shell.getMaximumSize().x = 1;
        assertThat(shell.getMaximumSize()).isEqualTo(new Point(900, 700));
    }

    @Test
    @DisplayName("a shell larger than its new maximum shrinks to it where it stands")
    void maximumShrinksTheShell() {
        shell.setMaximumSize(200, 300);
        assertThat(shell.getBounds()).isEqualTo(new Rectangle(10, 20, 200, 180));
    }
}
