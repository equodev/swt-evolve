package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/** The state {@link DartImage} holds for the wire, read through its generated accessors. */
@Tag("native-unit")
class ImageAccessorsNativeTest {

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
    @DisplayName("the accessors report what the image holds")
    void accessorsReportTheState() {
        ImageData data = new ImageData(3, 2, 8, new PaletteData(new RGB[] {new RGB(0, 0, 0), new RGB(255, 255, 255)}));
        data.transparentPixel = 1;
        Image image = new Image(display, data);
        DartImage impl = (DartImage) image.getImpl();
        Color background = new Color(display, 1, 2, 3);
        image.setBackground(background);

        assertThat(impl._width()).isEqualTo(3);
        assertThat(impl._height()).isEqualTo(2);
        assertThat(impl._background()).isSameAs(background);
        assertThat(impl._imageData().width).isEqualTo(3);
        assertThat(impl._remoteRef()).isNull();
        assertThat(impl._svgContent()).isNull();
        CompletableFuture<Void> future = new CompletableFuture<>();
        impl._pendingRenderFuture(future);
        assertThat(impl.pendingRenderFuture).isSameAs(future);
        impl._memGC(null);
        assertThat(impl._memGC()).isNull();
        image.dispose();
    }
}
