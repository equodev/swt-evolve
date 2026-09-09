package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.comm.CommService;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins {@code Display.getSystemColor(int)} against the native contract: an id the backend does not
 * map resolves to black ({@code Device.getSystemColor}'s default), never an exception. A throw here
 * escapes whatever listener asked for the colour — inside a {@code Paint} listener it takes the
 * display loop down with it.
 */
@Tag("flutter-it")
class DisplaySystemColorFallbackFlutterTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void unmappedIdIsBlack() {
        assertThat(rgba(display.getSystemColor(0)))
                .as("id 0 is not an SWT colour constant; native falls back to opaque black")
                .isEqualTo(new int[] { 0, 0, 0, 255 });
    }

    @Test
    void negativeAndOutOfRangeIdsAreBlack() {
        assertThat(rgba(display.getSystemColor(-1))).isEqualTo(new int[] { 0, 0, 0, 255 });
        assertThat(rgba(display.getSystemColor(9999))).isEqualTo(new int[] { 0, 0, 0, 255 });
    }

    @Test
    void mappedIdsKeepTheirColours() {
        assertThat(display.getSystemColor(SWT.COLOR_RED).getRGB()).isEqualTo(new RGB(255, 0, 0));
        assertThat(display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND).getRGB()).isEqualTo(new RGB(0, 0, 0));
        assertThat(rgba(display.getSystemColor(SWT.COLOR_TRANSPARENT)))
                .as("COLOR_TRANSPARENT is a mapped id and stays fully transparent")
                .isEqualTo(new int[] { 0, 0, 0, 0 });
    }

    private static int[] rgba(Color color) {
        return new int[] { color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() };
    }

    /** A stub injected only so {@code Display.init()} skips creating a real surface bridge. */
    private static final class NoopBridge extends FlutterBridge {
        final RecordingComm comm = new RecordingComm();

        NoopBridge() {
            clientReady.complete(true);
        }

        @Override
        protected CommService comm() {
            return comm;
        }

        @Override
        public void initFlutterView(Composite parent, DartControl control) {
        }

        @Override
        public void destroy(DartWidget control) {
        }
    }
}
