package dev.equo.swt.size;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** A vertical progress bar's natural size is the horizontal one turned: tall and narrow, not wide and short. */
@Tag("native-unit")
class ProgressBarOrientationTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("natural size follows the orientation")
    void naturalSizeFollowsTheOrientation() {
        Shell shell = DartMocks.dartShell();
        for (int extra : new int[] {SWT.NONE, SWT.SMOOTH, SWT.INDETERMINATE}) {
            Point horizontal = new ProgressBar(shell, SWT.HORIZONTAL | extra).computeSize(SWT.DEFAULT, SWT.DEFAULT);
            Point vertical = new ProgressBar(shell, SWT.VERTICAL | extra).computeSize(SWT.DEFAULT, SWT.DEFAULT);

            assertThat(horizontal.x).as("horizontal width, extra %d", extra).isGreaterThan(horizontal.y);
            assertThat(vertical).as("vertical, extra %d", extra).isEqualTo(new Point(horizontal.y, horizontal.x));
        }
    }
}
