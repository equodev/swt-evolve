package org.eclipse.swt.internal.image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

/**
 * Fallback used by {@code SVGFileFormat} when no {@code SVGRasterizer} service is available:
 * returns a transparent placeholder instead of crashing. When {@code captureMarkup} is set
 * (native/web tree) the raw SVG markup is stashed in {@link #pendingSvgContent} so DartImage
 * can hand it to Flutter for native rendering.
 */
public final class SVGFileFormatHelper {

    /** Raw SVG markup captured for the native/web tree; consumed by DartImage. */
    public static final ThreadLocal<String> pendingSvgContent = new ThreadLocal<>();

    private SVGFileFormatHelper() {
    }

    /** Placeholder sized from the SVG's own width/height or viewBox. */
    public static ImageData fallbackImageData(InputStream stream, boolean captureMarkup) {
        String svg = readSvgContent(stream);
        if (captureMarkup) {
            if (svg == null)
                SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT, null, " [No SVG rasterizer found]");
            pendingSvgContent.set(svg);
        }
        int[] dims = svg != null ? extractSvgDimensions(svg) : new int[] { 16, 16 };
        return placeholderImageData(dims[0], dims[1]);
    }

    /** Placeholder at an explicit size. */
    public static ImageData fallbackImageData(InputStream stream, int width, int height, boolean captureMarkup) {
        if (captureMarkup) {
            String svg = readSvgContent(stream);
            if (svg == null)
                SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT, null, " [No SVG rasterizer found]");
            pendingSvgContent.set(svg);
        }
        return placeholderImageData(width > 0 ? width : 16, height > 0 ? height : 16);
    }

    private static String readSvgContent(InputStream stream) {
        try {
            byte[] bytes = stream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private static ImageData placeholderImageData(int width, int height) {
        PaletteData palette = new PaletteData(0xFF0000, 0x00FF00, 0x0000FF);
        ImageData data = new ImageData(Math.max(1, width), Math.max(1, height), 24, palette);
        // fully transparent
        data.alphaData = new byte[data.width * data.height];
        return data;
    }

    private static int[] extractSvgDimensions(String svg) {
        int w = findIntAttribute(svg, "width");
        int h = findIntAttribute(svg, "height");
        if (w > 0 && h > 0)
            return new int[] { w, h };
        int vbIdx = svg.indexOf("viewBox");
        if (vbIdx >= 0) {
            for (char q : new char[] { '"', '\'' }) {
                int qs = svg.indexOf(q, vbIdx);
                if (qs >= 0) {
                    int qe = svg.indexOf(q, qs + 1);
                    if (qe > qs) {
                        String[] parts = svg.substring(qs + 1, qe).trim().split("\\s+");
                        if (parts.length == 4) {
                            try {
                                int vw = (int) Float.parseFloat(parts[2]);
                                int vh = (int) Float.parseFloat(parts[3]);
                                if (vw > 0 && vh > 0)
                                    return new int[] { vw, vh };
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        break;
                    }
                }
            }
        }
        return new int[] { 16, 16 };
    }

    private static int findIntAttribute(String svg, String attr) {
        for (char q : new char[] { '"', '\'' }) {
            String token = attr + "=" + q;
            int idx = svg.indexOf(token);
            if (idx >= 0) {
                int start = idx + token.length();
                int end = svg.indexOf(q, start);
                if (end > start) {
                    String val = svg.substring(start, end).trim().replaceAll("[^0-9.]", "");
                    try {
                        return (int) Float.parseFloat(val);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return 0;
    }
}
