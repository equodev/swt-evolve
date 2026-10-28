package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An image painted by an {@link ImageGcDrawer} equals another only with the same drawer, flag and size.
 *
 * <p>Split out because {@link ImageGcDrawer} only exists from SWT 3.129; the build excludes this file below
 * that (see swt_native/build.gradle.kts).
 */
@Tag("native-unit")
class ImageGcDrawerEqualityNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("drawer images are equal only to their own drawer, flag and size")
    void equalityByDrawer() {
        ImageGcDrawer drawer = (gc, width, height) -> gc.drawLine(0, 0, width, height);
        Image drawn = new Image(display, drawer, 5, 5);
        Image same = new Image(display, drawer, 5, 5);
        Image taller = new Image(display, drawer, 5, 6);
        Image other = new Image(display, (gc, width, height) -> gc.drawPoint(0, 0), 5, 5);
        Image grayDrawn = new Image(display, drawn, SWT.IMAGE_GRAY);
        Image plain = new Image(display, 5, 5);

        assertThat(drawn).isEqualTo(same);
        assertThat(drawn).isNotEqualTo(taller);
        assertThat(drawn).isNotEqualTo(other);
        assertThat(drawn).isNotEqualTo(grayDrawn);
        assertThat(drawn).isNotEqualTo(plain);
        for (Image image : new Image[] {drawn, same, taller, other, grayDrawn, plain})
            image.dispose();
    }
}
