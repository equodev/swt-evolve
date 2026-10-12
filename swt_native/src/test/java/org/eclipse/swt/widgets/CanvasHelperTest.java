package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Whether Canvas.scroll goes on past its argument checks follows the platform's SWT: Cocoa needs part of the
 * canvas left once its ancestors clip it (its view's visible rectangle), GTK needs the canvas visible, Win32
 * always goes on. The platform is passed in, so every rule is checked on any machine.
 */
@ExtendWith(Mocks.class)
class CanvasHelperTest {

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

    /** A 300x300 shell on screen. */
    private static Shell shell() {
        Shell shell = Mocks.shell();
        when(shell.getClientArea()).thenReturn(new Rectangle(0, 0, 300, 300));
        when(shell.isVisible()).thenReturn(true);
        return shell;
    }

    @Test
    @DisplayName("Cocoa: an empty canvas, or one its parent clips away, has nothing on screen")
    void cocoaNeedsAVisibleArea() {
        Shell shell = shell();
        Canvas empty = new Canvas(shell, SWT.NONE);
        assertThat(CanvasHelper.scrolls(empty, "cocoa")).isFalse();
        Composite parent = new Composite(shell, SWT.NONE);
        parent.setBounds(10, 10, 100, 100);
        Canvas outside = new Canvas(parent, SWT.NONE);
        outside.setBounds(150, 20, 50, 50);
        assertThat(CanvasHelper.scrolls(outside, "cocoa")).isFalse();
        Canvas partly = new Canvas(parent, SWT.NONE);
        partly.setBounds(80, 80, 50, 50);
        assertThat(CanvasHelper.scrolls(partly, "cocoa")).isTrue();
        Canvas inShell = new Canvas(shell, SWT.NONE);
        inShell.setBounds(0, 0, 20, 20);
        assertThat(CanvasHelper.scrolls(inShell, "cocoa")).isTrue();
    }

    @Test
    @DisplayName("Cocoa: a canvas in a shell off screen, or hidden, has nothing on screen")
    void cocoaNeedsTheCanvasOnScreen() {
        Shell shell = shell();
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 20, 20);
        canvas.setVisible(false);
        assertThat(CanvasHelper.scrolls(canvas, "cocoa")).isFalse();
        canvas.setVisible(true);
        assertThat(CanvasHelper.scrolls(canvas, "cocoa")).isTrue();
        when(shell.isVisible()).thenReturn(false);
        assertThat(CanvasHelper.scrolls(canvas, "cocoa")).isFalse();
    }

    @Test
    @DisplayName("Cocoa: an ancestor that is itself clipped away clips its whole subtree")
    void cocoaClipsThroughEveryAncestor() {
        Shell shell = shell();
        Composite outer = new Composite(shell, SWT.NONE);
        outer.setBounds(0, 0, 100, 100);
        Composite inner = new Composite(outer, SWT.NONE);
        inner.setBounds(200, 0, 100, 100);
        Canvas canvas = new Canvas(inner, SWT.NONE);
        canvas.setBounds(0, 0, 50, 50);
        assertThat(CanvasHelper.scrolls(canvas, "cocoa")).isFalse();
    }

    @Test
    @DisplayName("an empty canvas in a hidden shell moves none of its children when it scrolls")
    void scrollStopsAtTheGuard() {
        Assumptions.assumeFalse("win32".equals(SWT.getPlatform()), "Win32 scrolls whatever is on screen");
        Shell shell = shell();
        when(shell.isVisible()).thenReturn(false);
        when(shell.getImpl().isDrawing()).thenReturn(true);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        Composite child = new Composite(canvas, SWT.NONE);
        child.setBounds(0, 0, 10, 10);
        canvas.scroll(10, 0, 0, 0, 20, 20, true);
        assertThat(child.getBounds()).isEqualTo(new Rectangle(0, 0, 10, 10));
    }

    @Test
    @DisplayName("GTK follows the canvas's visibility; Win32 always scrolls")
    void gtkFollowsVisibilityAndWin32AlwaysScrolls() {
        Shell shell = shell();
        Canvas canvas = new Canvas(shell, SWT.NONE);
        assertThat(CanvasHelper.scrolls(canvas, "win32")).isTrue();
        when(shell.isVisible()).thenReturn(false);
        assertThat(CanvasHelper.scrolls(canvas, "gtk")).isFalse();
        when(shell.isVisible()).thenReturn(true);
        assertThat(CanvasHelper.scrolls(canvas, "gtk")).isTrue();
        canvas.setVisible(false);
        assertThat(CanvasHelper.scrolls(canvas, "gtk")).isFalse();
    }
}
