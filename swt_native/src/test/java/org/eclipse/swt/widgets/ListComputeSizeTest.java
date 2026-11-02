package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A List's preferred size is its content size plus its trim, as {@code computeTrim} reports it: a
 * hint replaces the content dimension, and an empty list with no hint is 64 high.
 */
@ExtendWith(Mocks.class)
class ListComputeSizeTest {

    private Composite parent;

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
        parent = new Composite(Mocks.shell(), SWT.NONE);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static Point trimmed(List list, int width, int height) {
        Rectangle trim = list.computeTrim(0, 0, width, height);
        return new Point(trim.width, trim.height);
    }

    @Test
    @DisplayName("hints replace the content size, and the trim is added around them")
    void hints() {
        for (int style : new int[] {SWT.NONE, SWT.BORDER, SWT.BORDER | SWT.V_SCROLL}) {
            List list = new List(parent, style);
            list.setItems(new String[] {"one", "two"});

            assertThat(list.computeSize(300, 60)).as("style %d", style).isEqualTo(trimmed(list, 300, 60));
            assertThat(list.computeSize(300, SWT.DEFAULT).x).as("style %d", style).isEqualTo(trimmed(list, 300, 0).x);
            assertThat(list.computeSize(SWT.DEFAULT, 60).y).as("style %d", style).isEqualTo(trimmed(list, 0, 60).y);
        }
    }

    @Test
    @DisplayName("an empty list with no hint is 64 high plus its trim")
    void emptyDefault() {
        for (int style : new int[] {SWT.NONE, SWT.BORDER, SWT.BORDER | SWT.V_SCROLL}) {
            List list = new List(parent, style);

            assertThat(list.computeSize(SWT.DEFAULT, SWT.DEFAULT).y).as("style %d", style)
                    .isEqualTo(trimmed(list, 0, 64).y);
        }
    }

    @Test
    @DisplayName("a 0 hint counts as the 64 default")
    void zeroHint() {
        List list = new List(parent, SWT.NONE);

        assertThat(list.computeSize(0, 0)).isEqualTo(trimmed(list, 64, 64));
    }

    @Test
    @DisplayName("the trim around the client area is the list's bounds")
    void clientAreaRoundTrip() {
        int[] styles = {SWT.NONE, SWT.BORDER, SWT.BORDER | SWT.V_SCROLL, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL};
        for (int style : styles) {
            List list = new List(parent, style);
            list.setBounds(10, 20, 317, 54);
            Rectangle client = list.getClientArea();

            Rectangle trim = list.computeTrim(client.x, client.y, client.width, client.height);

            assertThat(new Point(trim.width, trim.height)).as("style %d", style).isEqualTo(new Point(317, 54));
        }
    }
}
