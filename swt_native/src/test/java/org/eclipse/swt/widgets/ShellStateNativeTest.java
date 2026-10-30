package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** The minimum size, alpha, region, print and name of a shell answer as every platform's SWT does. The maximum size is in {@link ShellMaximumSizeNativeTest}. */
@Tag("native-unit")
class ShellStateNativeTest {

    private RecordingBridge bridge;
    private Shell shell;

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
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
    @DisplayName("a shell with no minimum given has a minimum of zero")
    void defaultMinimum() {
        assertThat(shell.getMinimumSize()).isEqualTo(new Point(0, 0));
    }

    @Test
    @DisplayName("the minimum is handed out as a copy")
    void minimumIsACopy() {
        shell.setMinimumSize(100, 80);
        shell.getMinimumSize().x = 1;
        assertThat(shell.getMinimumSize()).isEqualTo(new Point(100, 80));
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
    @DisplayName("only the low byte of an alpha counts")
    void alphaKeepsTheLowByte() {
        shell.setAlpha(300);
        assertThat(shell.getAlpha()).isEqualTo(44);
        shell.setAlpha(-1);
        assertThat(shell.getAlpha()).isEqualTo(255);
        shell.setAlpha(128);
        assertThat(shell.getAlpha()).isEqualTo(128);
    }

    @Test
    @DisplayName("setting the alpha a shell already has schedules no send")
    void sameAlphaIsNotSentAgain() {
        shell.setAlpha(128);
        bridge.dirtied.clear();
        shell.setAlpha(128);
        assertThat(bridge.dirtied).isEmpty();
        shell.setAlpha(64);
        assertThat(bridge.dirtied).isNotEmpty();
    }

    @Test
    @DisplayName("a disposed shell refuses setAlpha and setActive, as on Win32 and GTK")
    void disposedShellRefusesAlphaAndActive() {
        shell.dispose();
        assertThatThrownBy(() -> shell.setAlpha(10)).isInstanceOfSatisfying(SWTException.class,
                e -> assertThat(e.code).isEqualTo(SWT.ERROR_WIDGET_DISPOSED));
        assertThatThrownBy(shell::setActive).isInstanceOfSatisfying(SWTException.class,
                e -> assertThat(e.code).isEqualTo(SWT.ERROR_WIDGET_DISPOSED));
    }

    @Test
    @DisplayName("print refuses a disposed GC")
    void printRefusesADisposedGC() {
        GC gc = mock(GC.class);
        when(gc.isDisposed()).thenReturn(true);
        assertThatThrownBy(() -> shell.print(gc)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Argument not valid");
    }

    @Test
    @DisplayName("a shell names itself by its title")
    void toStringNamesTheTitle() {
        shell.setText("Title");
        assertThat(shell.toString()).startsWith("Shell {Title}");
    }

    // Shell.setRegion sizes a shell without trim to the region from SWT 3.105 (build 4629) on; earlier
    // versions keep the size, natively too.
    @Test
    @DisplayName("a shell without trim is as big as its region reaches, and one with trim ignores the region")
    void regionSizesTheShell() {
        assumeTrue(SWT.getVersion() >= 4629);
        Shell bare = shell(SWT.NO_TRIM);
        Region region = new Region(bare.getDisplay());
        region.add(10, 20, 100, 50);
        bare.setRegion(region);
        assertThat(bare.getSize()).isEqualTo(new Point(110, 70));

        shell.setRegion(region);
        assertThat(shell.getRegion()).isNull();
        assertThat(shell.getSize()).isEqualTo(new Point(250, 180));
    }
}
