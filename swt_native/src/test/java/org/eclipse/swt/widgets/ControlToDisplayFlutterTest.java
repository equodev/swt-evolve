package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where a control maps to on the display when its bridge does not say which shell it shows as its window: a
 * shell with a title bar drawn by Flutter adds the bar's height, unless it is taken for the main window (at the
 * origin, not modal, and filling most of the screen or 1024 by 768).
 */
@Tag("flutter-it")
class ControlToDisplayFlutterTest {

    private Display display;

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
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    private Point originOfAChild(int style, Rectangle bounds) {
        Shell shell = new Shell(display, style);
        shell.setBounds(bounds);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(5, 6, 10, 10);
        Point origin = canvas.toDisplay(0, 0);
        assertThat(canvas.toControl(origin)).isEqualTo(new Point(0, 0));
        return origin;
    }

    @Test
    void aTitledShellAwayFromTheOriginAddsItsTitleBar() {
        assertThat(originOfAChild(SWT.SHELL_TRIM, new Rectangle(10, 20, 300, 200))).isEqualTo(new Point(15, 56));
        assertThat(originOfAChild(SWT.CLOSE, new Rectangle(10, 20, 300, 200))).isEqualTo(new Point(15, 56));
    }

    @Test
    void aToolShellHasAShorterTitleBar() {
        assertThat(originOfAChild(SWT.TOOL | SWT.TITLE, new Rectangle(10, 20, 300, 200))).isEqualTo(new Point(15, 48));
    }

    @Test
    void aShellWithoutTitleBarAddsNothing() {
        assertThat(originOfAChild(SWT.NO_TRIM, new Rectangle(10, 20, 300, 200))).isEqualTo(new Point(15, 26));
        assertThat(originOfAChild(SWT.BORDER, new Rectangle(10, 20, 300, 200))).isEqualTo(new Point(15, 26));
    }

    @Test
    void aShellOfTheDefaultWindowSizeAtTheOriginIsTheMainWindow() {
        assertThat(originOfAChild(SWT.SHELL_TRIM, new Rectangle(0, 0, 1024, 768))).isEqualTo(new Point(5, 6));
    }

    @Test
    void aShellFillingMostOfTheScreenAtTheOriginIsTheMainWindow() {
        Rectangle screen = display.getMonitors()[0].getClientArea();
        assertThat(originOfAChild(SWT.SHELL_TRIM, new Rectangle(0, 0, screen.width, screen.height))).isEqualTo(new Point(5, 6));
    }

    @Test
    void aSmallOrModalShellAtTheOriginIsNotTheMainWindow() {
        assertThat(originOfAChild(SWT.SHELL_TRIM, new Rectangle(0, 0, 300, 200))).isEqualTo(new Point(5, 36));
        assertThat(originOfAChild(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL, new Rectangle(0, 0, 1024, 768))).isEqualTo(new Point(5, 36));
        assertThat(originOfAChild(SWT.SHELL_TRIM, new Rectangle(0, 5, 1024, 768))).isEqualTo(new Point(5, 41));
        assertThat(originOfAChild(SWT.SHELL_TRIM, new Rectangle(0, 0, 1024, 300))).isEqualTo(new Point(5, 36));
        Rectangle screen = display.getMonitors()[0].getClientArea();
        assertThat(originOfAChild(SWT.SHELL_TRIM, new Rectangle(0, 0, screen.width, 100))).isEqualTo(new Point(5, 36));
    }
}
