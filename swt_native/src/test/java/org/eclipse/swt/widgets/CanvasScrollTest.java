package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
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
 * Canvas.scroll goes on past its argument checks while the canvas is visible, on every platform: a hidden canvas
 * moves none of its children, and a shown one moves them even when its parents clip all of it away.
 */
@ExtendWith(Mocks.class)
class CanvasScrollTest {

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

    /** A 300x300 shell on screen that draws. */
    private static Shell shell() {
        Shell shell = Mocks.shell();
        when(shell.getClientArea()).thenReturn(new Rectangle(0, 0, 300, 300));
        when(shell.isVisible()).thenReturn(true);
        when(shell.getImpl().isDrawing()).thenReturn(true);
        return shell;
    }

    private static Composite child(Canvas canvas) {
        Composite child = new Composite(canvas, SWT.NONE);
        child.setBounds(0, 0, 10, 10);
        return child;
    }

    @Test
    @DisplayName("a canvas in a hidden shell, or hidden itself, moves none of its children")
    void aHiddenCanvasMovesNothing() {
        Shell shell = shell();
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 50, 50);
        Composite child = child(canvas);
        when(shell.isVisible()).thenReturn(false);
        canvas.scroll(10, 0, 0, 0, 20, 20, true);
        assertThat(child.getBounds()).as("in a hidden shell").isEqualTo(new Rectangle(0, 0, 10, 10));
        when(shell.isVisible()).thenReturn(true);
        canvas.setVisible(false);
        canvas.scroll(10, 0, 0, 0, 20, 20, true);
        assertThat(child.getBounds()).as("hidden").isEqualTo(new Rectangle(0, 0, 10, 10));
        canvas.setVisible(true);
        canvas.scroll(10, 0, 0, 0, 20, 20, true);
        assertThat(child.getBounds()).as("shown").isEqualTo(new Rectangle(10, 0, 10, 10));
    }

    @Test
    @DisplayName("a shown canvas moves its children even when it is empty or its parent clips it away")
    void aShownCanvasScrollsWhateverIsOnScreen() {
        Shell shell = shell();
        Canvas empty = new Canvas(shell, SWT.NONE);
        Composite inEmpty = child(empty);
        empty.scroll(10, 0, 0, 0, 20, 20, true);
        assertThat(inEmpty.getBounds()).as("an empty canvas").isEqualTo(new Rectangle(10, 0, 10, 10));
        Composite parent = new Composite(shell, SWT.NONE);
        parent.setBounds(10, 10, 100, 100);
        Canvas outside = new Canvas(parent, SWT.NONE);
        outside.setBounds(150, 20, 50, 50);
        Composite inOutside = child(outside);
        outside.scroll(10, 0, 0, 0, 20, 20, true);
        assertThat(inOutside.getBounds()).as("a canvas outside its parent").isEqualTo(new Rectangle(10, 0, 10, 10));
    }

    @Test
    @DisplayName("a canvas whose redraw, or its parent's, is off still moves its children")
    void aCanvasThatDoesNotDrawStillScrolls() {
        Shell shell = shell();
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 50, 50);
        Composite child = child(canvas);
        canvas.setRedraw(false);
        canvas.scroll(10, 0, 0, 0, 20, 20, true);
        assertThat(child.getBounds()).as("its own redraw off").isEqualTo(new Rectangle(10, 0, 10, 10));
        canvas.setRedraw(true);
        when(shell.getImpl().isDrawing()).thenReturn(false);
        canvas.scroll(20, 0, 10, 0, 20, 20, true);
        assertThat(child.getBounds()).as("its parent's redraw off").isEqualTo(new Rectangle(20, 0, 10, 10));
    }
}
