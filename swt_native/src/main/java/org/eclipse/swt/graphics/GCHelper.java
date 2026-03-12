package org.eclipse.swt.graphics;

import dev.equo.swt.FontMetricsUtil;
import dev.equo.swt.size.PointD;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class GCHelper {

    public static FontMetrics createFontMetrics(Font font) {
        Font resolved = font != null ? font : systemFont();
        int[] metrics = resolved != null ? FontMetricsUtil.computeFontMetrics(resolved) : null;
        DartFontMetrics impl = new DartFontMetrics(null);
        if (metrics != null) {
            impl.setMetrics(metrics[0], metrics[1], metrics[2], metrics[3]);
        }
        return impl.getApi();
    }

    private static Font systemFont() {
        Display display = Display.getCurrent();
        return display != null ? display.getSystemFont() : null;
    }

    /**
     * Computes the extent of a single line of text (no line breaks).
     * Applies DPI scaling so the result matches what the GC actually draws.
     */
    public static PointD computeLineExtent(String line, Font font) {
        PointD size = FontMetricsUtil.getFontSize(line, font);
        Display display = Display.getCurrent();
        if (display != null) {
            Point dpi = display.getDPI();
            if (dpi != null && dpi.x > 0) {
                double dpiScale = dpi.x / 72.0;
                return new PointD(size.x() * dpiScale, size.y() * dpiScale);
            }
        }
        return size;
    }

    /**
     * Computes the extent of the given string, processing flags for tab expansion,
     * line delimiters, and mnemonic characters.
     */
    public static Point textExtent(String string, int flags, Font font) {
        if (font == null) {
            font = Display.getCurrent().getSystemFont();
        }

        String text = string;

        // Process DRAW_MNEMONIC: remove '&' characters (mnemonic markers)
        if ((flags & SWT.DRAW_MNEMONIC) != 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '&' && i + 1 < text.length()) {
                    char next = text.charAt(i + 1);
                    if (next == '&') {
                        sb.append('&');
                        i++;
                    }
                    continue;
                }
                sb.append(c);
            }
            text = sb.toString();
        }

        // Process DRAW_TAB: replace tabs with spaces (8 spaces per tab)
        if ((flags & SWT.DRAW_TAB) != 0) {
            text = text.replace("\t", "        ");
        }

        // Process DRAW_DELIMITER: handle multiple lines
        if ((flags & SWT.DRAW_DELIMITER) != 0) {
            String[] lines = text.split("\r\n|\r|\n", -1);
            double maxWidth = 0;
            double totalHeight = 0;
            for (String line : lines) {
                PointD size = computeLineExtent(line, font);
                maxWidth = Math.max(maxWidth, size.x());
                totalHeight += size.y();
            }
            if (string.isEmpty()) {
                PointD size = computeLineExtent(" ", font);
                return new Point(0, (int) Math.round(size.y()));
            }
            return new Point((int) Math.round(maxWidth), (int) Math.round(totalHeight));
        }

        // No DRAW_DELIMITER: single line
        if (string.isEmpty()) {
            PointD size = computeLineExtent(" ", font);
            return new Point(0, (int) Math.round(size.y()));
        }
        return computeLineExtent(text, font).toPoint();
    }

    /**
     * Result of {@link #setupImageGC}, holding values that DartGC needs to set up
     * the bridge callback and assign to its fields.
     */
    public record ImageGCContext(
            Drawable resolvedDrawable,
            Image swtSource,
            Image dartImage,
            java.util.concurrent.CompletableFuture<Void> renderFuture) {}

    /**
     * Resolves the target image, sets memGC references and pendingRenderFutures.
     *
     * @return an {@link ImageGCContext} with the values DartGC must capture, or {@code null}
     *         if {@code drawable} is not image-backed.
     */
    public static ImageGCContext setupImageGC(Drawable drawable, GCData data, GC gcApi) {
        Image image = drawable instanceof Image img ? img : data.image;
        if (image == null) return null;

        data.image = image;
        Image swtSource = null;
        if (image.getImpl() instanceof DartImage di) {
            di.memGC = gcApi;
        } else if (image.getImpl() instanceof SwtImage si) {
            swtSource = image;
            Image dartCopy = GraphicsUtils.copyImage((Display) data.device, image);
            si.memGC = gcApi;
            ((DartImage) dartCopy.getImpl()).memGC = gcApi;
            data.image = dartCopy;
            drawable = dartCopy;
        }

        var renderFuture = new java.util.concurrent.CompletableFuture<Void>();
        if (data.image.getImpl() instanceof DartImage di) {
            di.pendingRenderFuture = renderFuture;
        }
        if (swtSource != null && swtSource.getImpl() instanceof SwtImage si) {
            si.pendingRenderFuture = renderFuture;
        }

        return new ImageGCContext(drawable, swtSource, (Image) data.image, renderFuture);
    }

    public static void updateImageFromPng(Image dartImage, Image swtSource, String pngBase64) {
        try {
            byte[] pngBytes = java.util.Base64.getDecoder().decode(pngBase64);
            ImageData newData = new ImageData(new java.io.ByteArrayInputStream(pngBytes));
            if (dartImage.getImpl() instanceof DartImage di) {
                di.updateImageData(newData);
            }
            if (swtSource != null && swtSource.getImpl() instanceof SwtImage si) {
                si.updateImageData(newData);
            }
        } catch (Exception e) {
            System.err.println("[GCHelper] Failed to update image from PNG: " + e.getMessage());
        }
    }
}