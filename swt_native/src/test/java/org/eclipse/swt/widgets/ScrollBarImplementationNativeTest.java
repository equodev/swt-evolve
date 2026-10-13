package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The bounds {@link DartScrollBar} reports for its track and thumb: where {@code canvas_evolve.dart} draws them,
 * relative to the parent. The parent here is not shown, so its client area keeps the bars' room.
 */
@Tag("native-unit")
class ScrollBarImplementationNativeTest {

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

    private Canvas canvas(int width, int height) {
        return canvas(SWT.H_SCROLL | SWT.V_SCROLL, width, height);
    }

    private Canvas canvas(int style, int width, int height) {
        Canvas canvas = new Canvas(shell, style);
        canvas.setBounds(0, 0, width, height);
        return canvas;
    }

    @Test
    @DisplayName("each track lies along the far edge of the parent's client area")
    void theTracksLieAlongTheFarEdges() {
        Canvas canvas = canvas(200, 150);
        Rectangle area = canvas.getClientArea();
        assertThat(canvas.getVerticalBar().getThumbTrackBounds())
                .isEqualTo(new Rectangle(area.x + area.width, area.y, 17, area.height));
        assertThat(canvas.getHorizontalBar().getThumbTrackBounds())
                .isEqualTo(new Rectangle(area.x, area.y + area.height, area.width, 17));
    }

    @Test
    @DisplayName("the thumb is as long as its share of the range, never under 16, and travels the rest of the track")
    void theThumbFollowsFlutter() {
        Canvas canvas = canvas(SWT.V_SCROLL, 200, 150);
        int x = canvas.getClientArea().width;
        ScrollBar vertical = canvas.getVerticalBar();
        vertical.setValues(0, 0, 100, 10, 1, 10);
        assertThat(vertical.getThumbBounds()).isEqualTo(new Rectangle(x, 0, 17, 16));
        vertical.setSelection(90);
        assertThat(vertical.getThumbBounds()).isEqualTo(new Rectangle(x, 134, 17, 16));
        vertical.setValues(25, 0, 100, 50, 1, 10);
        assertThat(vertical.getThumbBounds()).isEqualTo(new Rectangle(x, 38, 17, 75));

        Canvas wide = canvas(SWT.H_SCROLL, 200, 150);
        ScrollBar horizontal = wide.getHorizontalBar();
        horizontal.setValues(50, 0, 200, 100, 1, 10);
        assertThat(horizontal.getThumbBounds()).isEqualTo(new Rectangle(50, wide.getClientArea().height, 100, 17));
    }

    @Test
    @DisplayName("a track shorter than the thumb's floor is filled, and an empty one has no thumb")
    void aShortTrackIsFilled() {
        Canvas shortOne = canvas(SWT.V_SCROLL, 200, 10);
        assertThat(shortOne.getVerticalBar().getThumbBounds())
                .isEqualTo(new Rectangle(shortOne.getClientArea().width, 0, 17, 10));
        Canvas empty = canvas(SWT.V_SCROLL, 200, 0);
        assertThat(empty.getVerticalBar().getThumbBounds())
                .isEqualTo(new Rectangle(empty.getClientArea().width, 0, 17, 0));
    }

    @Test
    @DisplayName("setVisible shows and hides the bars of a List, a Text, a Table and a Tree too, as on GTK and Win32")
    void setVisibleChangesTheBarsOfEveryParent() {
        Shell shell = canvas(200, 150).getShell();
        Scrollable[] owners = {
                new List(shell, SWT.V_SCROLL), new Text(shell, SWT.MULTI | SWT.V_SCROLL),
                new Table(shell, SWT.V_SCROLL), new Tree(shell, SWT.V_SCROLL),
        };
        for (Scrollable owner : owners) {
            ScrollBar bar = owner.getVerticalBar();
            bar.setVisible(false);
            assertThat(bar.getVisible()).as(owner.getClass().getSimpleName()).isFalse();
            bar.setVisible(true);
            assertThat(bar.getVisible()).as(owner.getClass().getSimpleName()).isTrue();
        }
    }
}
