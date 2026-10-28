package org.eclipse.swt.graphics;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.image.SVGFileFormatHelper;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An image decoded from SVG: its markup travels to the render side, and a scalable file keeps a 200%
 * representation.
 *
 * <p>Split out because SWT reads SVG only in recent releases; the build excludes this file below 3.132
 * (see swt_native/build.gradle.kts).
 */
@Tag("native-unit")
class ImageSvgNativeTest {

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

    private static DartImage impl(Image image) {
        return (DartImage) image.getImpl();
    }

    private static ImageData direct(int width, int height) {
        return new ImageData(width, height, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
    }

    @Test
    @DisplayName("SVG markup a decode left behind travels with the image built from it")
    void svgMarkupIsPickedUp() {
        SVGFileFormatHelper.pendingSvgContent.set("<svg xmlns=\"http://www.w3.org/2000/svg\"/>");

        Image image = new Image(display, direct(2, 2));

        assertThat(impl(image)._svgContent()).isEqualTo("<svg xmlns=\"http://www.w3.org/2000/svg\"/>");
        assertThat(SVGFileFormatHelper.pendingSvgContent.get()).isNull();
        image.dispose();
    }

    @Test
    @DisplayName("an SVG file is scalable, so its image keeps a 200% representation its copies inherit")
    void svgFileKeepsA200Representation() throws Exception {
        File svg = File.createTempFile("imageimpl", ".svg");
        svg.deleteOnExit();
        Files.write(svg.toPath(), "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"4\" height=\"4\"/>".getBytes());
        ImageFileNameProvider provider = zoom -> zoom == 100 ? svg.getAbsolutePath() : null;

        Image image = new Image(display, provider);
        Image copy = new Image(display, image, SWT.IMAGE_COPY);

        assertThat(copy).isEqualTo(image);
        assertThat(copy.getBounds()).isEqualTo(new Rectangle(0, 0, 4, 4));
        copy.dispose();
        image.dispose();
    }
}
