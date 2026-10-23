package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Scrolling sideways with a tilt wheel, a horizontal touchpad swipe or Shift + wheel, driven by
 * real browser wheel events. As on Windows: a horizontal wheel moves the horizontal bar and is
 * reported as SWT.MouseHorizontalWheel, and Shift turns a vertical wheel sideways.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CanvasHorizontalWheelFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 400, 300);
        shell.open();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    @AfterEach
    void clear() {
        for (Control c : shell.getChildren()) c.dispose();
        flutter.settle();
    }

    /** A Canvas whose content is larger than it is both ways, as a diagram editor's. */
    private Canvas scrollingCanvas(int style) {
        Canvas canvas = new Canvas(shell, style);
        canvas.setBounds(10, 10, 300, 200);
        if (canvas.getHorizontalBar() != null) canvas.getHorizontalBar().setValues(0, 0, 1000, 100, 10, 100);
        if (canvas.getVerticalBar() != null) canvas.getVerticalBar().setValues(0, 0, 1000, 100, 10, 100);
        flutter.settle();
        return canvas;
    }

    private void wheel(Control over, double deltaX, double deltaY, int modifiers) {
        double[] r = flutter.renderedRect(over);
        flutter.input().wheel(r[0] + 50, r[1] + 50, deltaX, deltaY, modifiers);
        flutter.settle();
    }

    @Test
    @DisplayName("a horizontal wheel scrolls a Canvas's horizontal bar and leaves the vertical one")
    void horizontalWheelScrollsTheHorizontalBar() {
        Canvas canvas = scrollingCanvas(SWT.H_SCROLL | SWT.V_SCROLL);

        wheel(canvas, 120, 0, 0);

        assertThat(canvas.getHorizontalBar().getSelection()).isPositive();
        assertThat(canvas.getVerticalBar().getSelection()).isZero();

        wheel(canvas, -120, 0, 0);

        assertThat(canvas.getHorizontalBar().getSelection()).isZero();
    }

    @Test
    @DisplayName("a Canvas with bars reports the horizontal wheel, and a veto keeps its bar still")
    void vetoedHorizontalWheelLeavesTheBar() {
        Canvas canvas = scrollingCanvas(SWT.H_SCROLL | SWT.V_SCROLL);
        List<Event> horizontal = new ArrayList<>();
        canvas.addListener(SWT.MouseHorizontalWheel, e -> {
            horizontal.add(e);
            e.doit = false;
        });

        wheel(canvas, 120, 0, 0);

        assertThat(horizontal).hasSize(1);
        assertThat(canvas.getHorizontalBar().getSelection()).isZero();
    }

    @Test
    @DisplayName("Shift + wheel over a Canvas carries SHIFT in its state mask")
    void shiftWheelCarriesTheStateMask() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(10, 10, 300, 200);
        List<Event> horizontal = new ArrayList<>();
        canvas.addListener(SWT.MouseHorizontalWheel, horizontal::add);
        flutter.settle();

        wheel(canvas, 0, 120, SWT.SHIFT);

        assertThat(horizontal).hasSize(1);
        assertThat(horizontal.get(0).stateMask & SWT.SHIFT).isEqualTo(SWT.SHIFT);
    }

    @Test
    @DisplayName("Shift + wheel scrolls a Canvas sideways")
    void shiftWheelScrollsSideways() {
        Canvas canvas = scrollingCanvas(SWT.H_SCROLL | SWT.V_SCROLL);

        wheel(canvas, 0, 120, SWT.SHIFT);

        assertThat(canvas.getHorizontalBar().getSelection()).isPositive();
        assertThat(canvas.getVerticalBar().getSelection()).isZero();
    }

    @Test
    @DisplayName("control: a vertical wheel scrolls the vertical bar only")
    void verticalWheelScrollsTheVerticalBar() {
        Canvas canvas = scrollingCanvas(SWT.H_SCROLL | SWT.V_SCROLL);

        wheel(canvas, 0, 120, 0);

        assertThat(canvas.getVerticalBar().getSelection()).isPositive();
        assertThat(canvas.getHorizontalBar().getSelection()).isZero();
    }

    @Test
    @DisplayName("a horizontal wheel over a Canvas with nothing to scroll is SWT.MouseHorizontalWheel")
    void horizontalWheelReachesListenersOfACanvas() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(10, 10, 300, 200);
        List<Event> vertical = new ArrayList<>(), horizontal = new ArrayList<>();
        canvas.addListener(SWT.MouseWheel, vertical::add);
        canvas.addListener(SWT.MouseHorizontalWheel, horizontal::add);
        flutter.settle();

        wheel(canvas, 120, 0, 0);

        assertThat(vertical).isEmpty();
        assertThat(horizontal).hasSize(1);
        assertThat(horizontal.get(0).count).as("positive is left, as on the platform").isNegative();
    }

    @Test
    @DisplayName("a horizontal wheel over a Composite's own area is SWT.MouseHorizontalWheel")
    void horizontalWheelReachesListenersOfAComposite() {
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setBounds(10, 10, 300, 200);
        new Label(composite, SWT.NONE).setBounds(0, 180, 300, 20);
        List<Event> horizontal = new ArrayList<>();
        composite.addListener(SWT.MouseHorizontalWheel, horizontal::add);
        flutter.settle();

        wheel(composite, -120, 0, 0);

        assertThat(horizontal).hasSize(1);
        assertThat(horizontal.get(0).count).isPositive();
    }

    @Test
    @DisplayName("a horizontal wheel over a Composite's own area moves its horizontal bar")
    void horizontalWheelMovesACompositesBar() {
        Composite composite = new Composite(shell, SWT.H_SCROLL);
        composite.setBounds(10, 10, 300, 200);
        new Label(composite, SWT.NONE).setBounds(0, 160, 300, 20);
        composite.getHorizontalBar().setValues(0, 0, 1000, 100, 10, 100);
        List<Event> selections = new ArrayList<>();
        composite.getHorizontalBar().addListener(SWT.Selection, selections::add);
        flutter.settle();

        wheel(composite, 120, 0, 0);

        assertThat(composite.getHorizontalBar().getSelection()).isPositive();
        assertThat(selections).isNotEmpty();
    }
}
