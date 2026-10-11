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
        Canvas canvas = new Canvas(shell, SWT.H_SCROLL | SWT.V_SCROLL);
        canvas.setBounds(0, 0, width, height);
        return canvas;
    }

    @Test
    @DisplayName("each track lies along the far edge of the parent's client area")
    void theTracksLieAlongTheFarEdges() {
        Canvas canvas = canvas(200, 150);
        assertThat(canvas.getVerticalBar().getThumbTrackBounds()).isEqualTo(new Rectangle(200, 0, 17, 150));
        assertThat(canvas.getHorizontalBar().getThumbTrackBounds()).isEqualTo(new Rectangle(0, 150, 200, 17));
    }

    @Test
    @DisplayName("the thumb is as long as its share of the range, never under 16, and travels the rest of the track")
    void theThumbFollowsFlutter() {
        Canvas canvas = canvas(200, 150);
        ScrollBar vertical = canvas.getVerticalBar();
        vertical.setValues(0, 0, 100, 10, 1, 10);
        assertThat(vertical.getThumbBounds()).isEqualTo(new Rectangle(200, 0, 17, 16));
        vertical.setSelection(90);
        assertThat(vertical.getThumbBounds()).isEqualTo(new Rectangle(200, 134, 17, 16));
        vertical.setValues(25, 0, 100, 50, 1, 10);
        assertThat(vertical.getThumbBounds()).isEqualTo(new Rectangle(200, 38, 17, 75));

        ScrollBar horizontal = canvas.getHorizontalBar();
        horizontal.setValues(50, 0, 200, 100, 1, 10);
        assertThat(horizontal.getThumbBounds()).isEqualTo(new Rectangle(50, 150, 100, 17));
    }

    @Test
    @DisplayName("a track shorter than the thumb's floor is filled, and an empty one has no thumb")
    void aShortTrackIsFilled() {
        ScrollBar bar = canvas(200, 10).getVerticalBar();
        assertThat(bar.getThumbBounds()).isEqualTo(new Rectangle(200, 0, 17, 10));
        ScrollBar none = canvas(200, 0).getVerticalBar();
        assertThat(none.getThumbBounds()).isEqualTo(new Rectangle(200, 0, 17, 0));
    }
}
