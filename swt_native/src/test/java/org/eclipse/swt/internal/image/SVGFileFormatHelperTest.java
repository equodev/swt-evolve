package org.eclipse.swt.internal.image;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Where no SVG rasterizer is installed, an SVG decodes to a transparent placeholder of the size the
 * markup declares, and the markup itself goes on to the render side.
 */
@Tag("native-unit")
class SVGFileFormatHelperTest {

    @BeforeEach
    @AfterEach
    void clearMarkup() {
        SVGFileFormatHelper.pendingSvgContent.remove();
    }

    private static InputStream svg(String markup) {
        return new ByteArrayInputStream(markup.getBytes(StandardCharsets.UTF_8));
    }

    private static ImageData decode(String markup) {
        return SVGFileFormatHelper.fallbackImageData(svg(markup), false);
    }

    private static void assertSize(ImageData data, int width, int height) {
        assertThat(data.width).isEqualTo(width);
        assertThat(data.height).isEqualTo(height);
    }

    @Test
    @DisplayName("the placeholder takes the size the width and height attributes give, in either quotes")
    void sizeFromWidthAndHeight() {
        ImageData data = decode("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"30px\" height=\"12\"/>");
        assertSize(data, 30, 12);
        assertThat(data.alphaData).as("fully transparent").containsOnly((byte) 0).hasSize(30 * 12);

        assertSize(decode("<svg xmlns='http://www.w3.org/2000/svg' width='7' height='5'/>"), 7, 5);
    }

    @Test
    @DisplayName("without a usable width and height the placeholder takes the viewBox's size, else 16x16")
    void sizeFromViewBoxOrDefault() {
        assertSize(decode("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 40.5 20\"/>"), 40, 20);
        assertSize(decode("<svg xmlns='http://www.w3.org/2000/svg' width='px' viewBox='0 0 8 6'/>"), 8, 6);
        assertSize(decode("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"\" viewBox=\"0 0 8\"/>"), 16, 16);
        assertSize(decode("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"30\" viewBox=\"0 0 8 6\"/>"), 8, 6);
        assertSize(decode("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 0 6\"/>"), 16, 16);
        assertSize(decode("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 6 0\"/>"), 16, 16);
        assertSize(decode("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 a b\"/>"), 16, 16);
        assertSize(decode("<svg xmlns=\"http://www.w3.org/2000/svg\"><desc>viewBox \"8 6</desc></svg>"), 16, 16);
        assertSize(decode("<svg xmlns=\"http://www.w3.org/2000/svg\"><desc>viewBox</desc></svg>"), 16, 16);
        assertSize(decode("<svg xmlns=\"http://www.w3.org/2000/svg\"/>"), 16, 16);
    }

    @Test
    @DisplayName("markup that is not well-formed SVG is an invalid image")
    void notSvgIsInvalid() {
        assertThatThrownBy(() -> decode("<svg"))
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_INVALID_IMAGE));
        assertThatThrownBy(() -> decode("<html/>"))
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_INVALID_IMAGE));
        assertSize(decode("<s:svg xmlns:s=\"http://www.w3.org/2000/svg\" width=\"3\" height=\"2\"/>"), 3, 2);
    }

    @Test
    @DisplayName("the markup is kept for the render side when asked, and an unreadable stream then has none to keep")
    void markupIsCaptured() {
        String markup = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"4\" height=\"4\"/>";
        decode(markup);
        assertThat(SVGFileFormatHelper.pendingSvgContent.get()).isNull();

        SVGFileFormatHelper.fallbackImageData(svg(markup), true);
        assertThat(SVGFileFormatHelper.pendingSvgContent.get()).isEqualTo(markup);

        assertSize(SVGFileFormatHelper.fallbackImageData(unreadable(), false), 16, 16);
        assertThatThrownBy(() -> SVGFileFormatHelper.fallbackImageData(unreadable(), true))
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_UNSUPPORTED_FORMAT));
    }

    @Test
    @DisplayName("decoded at a size, the placeholder has that size, 16 for one not given, and the markup is kept when asked")
    void decodedAtASize() {
        String markup = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>";
        assertSize(SVGFileFormatHelper.fallbackImageData(unreadable(), 9, 3, false), 9, 3);
        assertSize(SVGFileFormatHelper.fallbackImageData(unreadable(), 0, -1, false), 16, 16);
        assertThat(SVGFileFormatHelper.pendingSvgContent.get()).isNull();

        assertSize(SVGFileFormatHelper.fallbackImageData(svg(markup), 5, 5, true), 5, 5);
        assertThat(SVGFileFormatHelper.pendingSvgContent.get()).isEqualTo(markup);
        assertThatThrownBy(() -> SVGFileFormatHelper.fallbackImageData(svg("<html/>"), 5, 5, true))
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_INVALID_IMAGE));
        assertThatThrownBy(() -> SVGFileFormatHelper.fallbackImageData(unreadable(), 5, 5, true))
                .isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(SWT.ERROR_UNSUPPORTED_FORMAT));
    }

    private static InputStream unreadable() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("unreadable");
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                throw new IOException("unreadable");
            }
        };
    }
}
