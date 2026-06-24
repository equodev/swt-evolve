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
        if (!dev.equo.swt.FlutterBridge.displayBootstrapped) return null;
        Image image = drawable instanceof Image img ? img : data.image;
        if (image == null) return null;

        data.image = image;
        Image swtSource = null;
        if (image.getImpl() instanceof DartImage di) {
            di.memGC = gcApi;
        } else if (!(image.getImpl() instanceof DartImage)) {
            swtSource = image;
            Image dartCopy = GraphicsUtils.copyImage((Display) data.device, image);
            image.getImpl()._memGC(gcApi);
            ((DartImage) dartCopy.getImpl()).memGC = gcApi;
            data.image = dartCopy;
            drawable = dartCopy;
        }

        var renderFuture = new java.util.concurrent.CompletableFuture<Void>();
        if (data.image.getImpl() instanceof DartImage di) {
            di.pendingRenderFuture = renderFuture;
        }
        if (swtSource != null && !(swtSource.getImpl() instanceof DartImage)) {
            swtSource.getImpl()._pendingRenderFuture(renderFuture);
        }

        return new ImageGCContext(drawable, swtSource, (Image) data.image, renderFuture);
    }

    /**
     * Updates an image from raw PNG bytes received over the binary comm channel — the desktop
     * D→J image path. No base64 is involved (the bytes arrive verbatim via {@code sendBytes}).
     */
    public static void updateImageFromPngBytes(Image dartImage, Image swtSource, byte[] pngBytes) {
        if (pngBytes == null) return;
        try {
            ImageData newData = new ImageData(new java.io.ByteArrayInputStream(pngBytes));
            if (dartImage.getImpl() instanceof DartImage di) {
                di._updateImageData(newData);
            }
            if (swtSource != null && !(swtSource.getImpl() instanceof DartImage)) {
                swtSource.getImpl()._updateImageData(newData);
            }
        } catch (Exception e) {
            System.err.println("[GCHelper] Failed to update image from PNG bytes: " + e.getMessage());
        }
    }

    /**
     * Base64 variant — retained for the web (Chromium) transport, whose raw-bytes channel is not
     * yet implemented. Desktop uses {@link #updateImageFromPngBytes} instead.
     */
    public static void updateImageFromPng(Image dartImage, Image swtSource, String pngBase64) {
        if (pngBase64 == null) return;
        updateImageFromPngBytes(dartImage, swtSource, java.util.Base64.getDecoder().decode(pngBase64));
    }

    private static final String RESPONSE_SUFFIX = "Response";

    /**
     * Blocking request/response over the Flutter bridge, safe to call from the SWT UI thread.
     * Sends {@code eventName} with {@code args}, then pumps the SWT event loop until the
     * {@code eventName + "Response"} payload (raw frame bytes) arrives or {@code timeoutMs} elapses,
     * handing the bytes to {@code handler}. Pumping (instead of {@code future.get()}) keeps the UI
     * responsive and lets the inbound WebSocket frame be dispatched. Used by the binary D→J image
     * path where Flutter returns PNG bytes via {@code sendBytes} (e.g. {@code GC.copyArea(Image,…)}).
     */
    public static void callOnDisplayBytes(Object widget, String eventName, Object args,
                                          java.util.function.Consumer<byte[]> handler, long timeoutMs) {
        Display display = displayOf(widget);
        String receiveEvent = eventName + RESPONSE_SUFFIX;
        var future = new java.util.concurrent.CompletableFuture<Void>();
        dev.equo.swt.FlutterBridge.onPayload(widget, receiveEvent, p -> {
            dev.equo.swt.FlutterBridge.removeEvent(widget, receiveEvent);
            handler.accept(p);
            future.complete(null);
            if (display != null && !display.isDisposed()) display.wake();
        });
        if (widget instanceof org.eclipse.swt.widgets.DartWidget w)
            dev.equo.swt.FlutterBridge.send(w, eventName, args);
        else if (widget instanceof DartResource r)
            dev.equo.swt.FlutterBridge.send(r, eventName, args);
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!future.isDone() && System.currentTimeMillis() < deadline) {
            if (display != null && !display.isDisposed() && !display.readAndDispatch()) {
                display.sleep();
            }
        }
        if (!future.isDone()) dev.equo.swt.FlutterBridge.removeEvent(widget, receiveEvent);
    }

    private static Display displayOf(Object widget) {
        if (widget instanceof org.eclipse.swt.widgets.DartWidget w) return w.getDisplay();
        if (widget instanceof DartGC gc) return gc.getDisplay();
        return null;
    }
}