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

/** The size limits, alpha and region of a shell answer as every platform's SWT does. */
@Tag("native-unit")
class ShellStateNativeTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = shell(SWT.SHELL_TRIM);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static Shell shell(int style) {
        Display display = DartMocks.dartDisplay();
        when(((DartDisplay) display.getImpl()).isValidThread()).thenReturn(true);
        when(display.getShells()).thenReturn(new Shell[0]);
        Shell shell = new Shell(display, style);
        shell.setBounds(10, 20, 250, 180);
        return shell;
    }

    @Test
    @DisplayName("a shell with no limit given has a minimum of zero and a maximum nothing can reach")
    void defaultLimits() {
        assertThat(shell.getMinimumSize()).isEqualTo(new Point(0, 0));
        assertThat(shell.getMaximumSize()).isEqualTo(new Point(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("the limits are handed out as copies")
    void limitsAreCopies() {
        shell.setMinimumSize(100, 80);
        shell.setMaximumSize(900, 700);
        shell.getMinimumSize().x = 1;
        shell.getMaximumSize().x = 1;
        assertThat(shell.getMinimumSize()).isEqualTo(new Point(100, 80));
        assertThat(shell.getMaximumSize()).isEqualTo(new Point(900, 700));
    }

    @Test
    @DisplayName("a shell smaller than its new minimum grows to it where it stands")
    void minimumGrowsTheShell() {
        shell.setMinimumSize(400, 150);
        assertThat(shell.getBounds()).isEqualTo(new Rectangle(10, 20, 400, 180));
    }

    @Test
    @DisplayName("a size asked for below the minimum is raised to it on each axis")
    void sizeBelowTheMinimumIsRaised() {
        shell.setMinimumSize(300, 200);
        shell.setSize(100, 400);
        assertThat(shell.getSize()).isEqualTo(new Point(300, 400));
        shell.setBounds(5, 6, 500, 50);
        assertThat(shell.getBounds()).isEqualTo(new Rectangle(5, 6, 500, 200));
    }

    @Test
    @DisplayName("moving a shell never resizes it")
    void movingKeepsTheSize() {
        shell.setMinimumSize(200, 100);
        shell.setLocation(70, 80);
        assertThat(shell.getBounds()).isEqualTo(new Rectangle(70, 80, 250, 180));
    }

    @Test
    @DisplayName("a shell larger than its new maximum shrinks to it where it stands")
    void maximumShrinksTheShell() {
        shell.setMaximumSize(200, 300);
        assertThat(shell.getBounds()).isEqualTo(new Rectangle(10, 20, 200, 180));
    }

}
