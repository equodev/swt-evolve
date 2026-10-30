package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * The client draws a visible scroll bar only while it has a range to scroll, and the content fills
 * the place of one it does not draw. The client area has to say the same: a JFace ruler paints the
 * lines of its editor's client area and scrolls by copying what it painted, so a bar counted but not
 * drawn left rows it never painted to be copied into view.
 */
@Tag("native-unit")
class ScrollBarRoomTest {

    private Canvas canvas;
    private final AtomicInteger resizes = new AtomicInteger();

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        when(DartMocks.dartDisplay().getShells()).thenReturn(new Shell[0]);
        Shell shell = DartMocks.dartShell();
        // A bar counts only when it is showing, which needs a shell that is.
        when(shell.isVisible()).thenReturn(true);
        canvas = new Canvas(shell, SWT.H_SCROLL | SWT.V_SCROLL);
        canvas.setBounds(0, 0, 200, 100);
        canvas.addListener(SWT.Resize, e -> {});
        // The mocked display delivers nothing; count the resizes it is handed for the canvas.
        DartDisplay displayImpl = displayImpl();
        doAnswer(inv -> {
            Event event = inv.getArgument(1);
            if (event.type == SWT.Resize && event.widget == canvas) resizes.incrementAndGet();
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
    }

    private static DartDisplay displayImpl() {
        return (DartDisplay) DartMocks.dartDisplay().getImpl();
    }

    @AfterEach
    void tearDown() {
        DartDisplay displayImpl = displayImpl();
        doNothing().when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        canvas.dispose();
        when(DartMocks.dartDisplay().getShells()).thenReturn(null);
        FlutterBridge.set(null);
    }

    @Test
    void aBarWithARangeTakesItsRoom() {
        Rectangle area = canvas.getClientArea();
        assertThat(area.width).isEqualTo(200 - Sizes.SCROLL_BAR_SIZE);
        assertThat(area.height).isEqualTo(100 - Sizes.SCROLL_BAR_SIZE);
    }

    @Test
    void aBarWithNothingToScrollLeavesItsRoomToTheContent() {
        canvas.getHorizontalBar().setValues(0, 0, 1, 1, 1, 1);

        Rectangle area = canvas.getClientArea();
        assertThat(area.height).as("no horizontal bar drawn").isEqualTo(100);
        assertThat(area.width).as("the vertical bar still scrolls").isEqualTo(200 - Sizes.SCROLL_BAR_SIZE);
        assertThat(resizes).as("the client area grew").hasValue(1);
    }

    @Test
    void aBarThatGainsARangeTakesItsRoomBack() {
        ScrollBar bar = canvas.getHorizontalBar();
        bar.setValues(0, 0, 1, 1, 1, 1);
        resizes.set(0);

        bar.setMaximum(500);

        assertThat(canvas.getClientArea().height).isEqualTo(100 - Sizes.SCROLL_BAR_SIZE);
        assertThat(resizes).as("the client area shrank").hasValue(1);
    }

    @Test
    void aRangeChangeThatKeepsTheBarDrawnResizesNothing() {
        canvas.getVerticalBar().setValues(0, 0, 1000, 80, 1, 10);
        canvas.getVerticalBar().setThumb(90);

        assertThat(resizes).hasValue(0);
    }

    @Test
    void aHiddenBarTakesNoRoomSoItsRangeResizesNothing() {
        ScrollBar bar = canvas.getHorizontalBar();
        bar.setVisible(false);
        resizes.set(0);

        bar.setValues(0, 0, 1, 1, 1, 1);
        bar.setMaximum(500);

        assertThat(canvas.getClientArea().height).isEqualTo(100);
        assertThat(resizes).hasValue(0);
    }
}
