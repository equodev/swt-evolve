package dev.equo.swt.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.PixelGrabber;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;

/**
 * Shows the cursor of the Swing component under the pointer on the canvas an embedding is painted
 * into. The off-screen frame has no native window for the OS to ask for a cursor, so nothing else
 * sets one there, and the canvas shows whatever its ancestors do.
 *
 * <p>The component is resolved from the last SWT pointer position, not from the OS pointer the frame
 * itself would use: over a web client the server's pointer is not the user's. As natively, a move
 * re-resolves the cursor except while a button is held, and a component changing its cursor
 * re-resolves it at any time.
 *
 * <p>A custom cursor becomes an SWT cursor made from its image. AWT keeps that image but not its
 * hotspot, which only the platform can read back ({@code NativeCursorHotspot}); where it cannot,
 * the hotspot is the top-left corner.
 */
final class EmbedCursor {

    private static final Method NATIVE_HOTSPOT = nativeHotspotMethod();

    private final Canvas canvas;
    private final Display display;
    private final Container root;
    // Where the pointer is over the canvas, in frame coordinates; null while it is elsewhere.
    private volatile Point pointer;
    // What the canvas was last given: an SWT.CURSOR_* constant, or the custom AWT cursor itself.
    private final AtomicReference<Object> shown = new AtomicReference<>();
    // SWT thread only, both.
    private final Map<Cursor, org.eclipse.swt.graphics.Cursor> customCursors = new WeakHashMap<>();
    private boolean pressed;

    EmbedCursor(Canvas canvas, Display display, Container root) {
        this.canvas = canvas;
        this.display = display;
        this.root = root;
    }

    /**
     * Subscribes to the canvas's pointer events. Must be called after {@link AwtInput#attach}: each
     * update is queued behind the dispatch of the event it follows, so a component that changes its
     * cursor in response to that event is already showing the new one.
     */
    void attach() {
        Listener l = e -> {
            switch (e.type) {
                case SWT.MouseDown:
                    pressed = true;
                    break;
                case SWT.MouseUp:
                    pressed = false;
                    break;
                case SWT.MouseEnter:
                case SWT.MouseMove: {
                    final int x = e.x, y = e.y;
                    final boolean dragging = pressed;
                    EventQueue.invokeLater(() -> moved(x, y, dragging));
                    break;
                }
                case SWT.MouseExit:
                    EventQueue.invokeLater(() -> pointer = null);
                    break;
                case SWT.Dispose:
                    customCursors.values().forEach(org.eclipse.swt.graphics.Cursor::dispose);
                    customCursors.clear();
                    break;
            }
        };
        canvas.addListener(SWT.MouseDown, l);
        canvas.addListener(SWT.MouseUp, l);
        canvas.addListener(SWT.MouseEnter, l);
        canvas.addListener(SWT.MouseMove, l);
        canvas.addListener(SWT.MouseExit, l);
        canvas.addListener(SWT.Dispose, l);
    }

    void moved(int x, int y, boolean dragging) {
        pointer = new Point(x, y);
        if (!dragging) refresh();
    }

    /** Shows the cursor of the component now under the pointer, if the pointer is over the canvas. */
    void refresh() {
        Point p = pointer;
        if (p == null) return;
        show(cursorAt(root, p.x, p.y));
    }

    private void show(Cursor cursor) {
        Object key = isCustom(cursor) ? cursor : (Object) swtCursor(cursor);
        if (Objects.equals(shown.getAndSet(key), key) || display.isDisposed()) return;
        try {
            display.asyncExec(() -> {
                if (!canvas.isDisposed()) canvas.setCursor(toSwt(cursor));
            });
        } catch (SWTException disposed) {
            // The display went away between the check and the post.
        }
    }

    private org.eclipse.swt.graphics.Cursor toSwt(Cursor cursor) {
        if (isCustom(cursor)) {
            org.eclipse.swt.graphics.Cursor custom =
                    customCursors.computeIfAbsent(cursor, this::customToSwt);
            if (custom != null) return custom;
        }
        return display.getSystemCursor(swtCursor(cursor));
    }

    private org.eclipse.swt.graphics.Cursor customToSwt(Cursor cursor) {
        ImageData data = imageDataOf(customImage(cursor));
        if (data == null) return null;
        int[] hotspot = nativeHotspot(cursor, data.width, data.height);
        int x = hotspot != null ? Math.max(0, Math.min(hotspot[0], data.width - 1)) : 0;
        int y = hotspot != null ? Math.max(0, Math.min(hotspot[1], data.height - 1)) : 0;
        return new org.eclipse.swt.graphics.Cursor(display, data, x, y);
    }

    /** The cursor of the component at {@code (x, y)}, inherited from its ancestors if it sets none. */
    static Cursor cursorAt(Container root, int x, int y) {
        Component c = SwingUtilities.getDeepestComponentAt(root, x, y);
        return c != null ? c.getCursor() : null;
    }

    static boolean isCustom(Cursor cursor) {
        return cursor != null && cursor.getType() == Cursor.CUSTOM_CURSOR;
    }

    /**
     * The {@code SWT.CURSOR_*} for an AWT cursor. One-way resize cursors become the two-way ones,
     * which is also what Windows draws for them; a custom cursor's fallback is the arrow.
     */
    static int swtCursor(Cursor cursor) {
        if (cursor == null) return SWT.CURSOR_ARROW;
        switch (cursor.getType()) {
            case Cursor.CROSSHAIR_CURSOR: return SWT.CURSOR_CROSS;
            case Cursor.TEXT_CURSOR: return SWT.CURSOR_IBEAM;
            case Cursor.WAIT_CURSOR: return SWT.CURSOR_WAIT;
            case Cursor.HAND_CURSOR: return SWT.CURSOR_HAND;
            case Cursor.MOVE_CURSOR: return SWT.CURSOR_SIZEALL;
            case Cursor.N_RESIZE_CURSOR:
            case Cursor.S_RESIZE_CURSOR: return SWT.CURSOR_SIZENS;
            case Cursor.E_RESIZE_CURSOR:
            case Cursor.W_RESIZE_CURSOR: return SWT.CURSOR_SIZEWE;
            case Cursor.NE_RESIZE_CURSOR:
            case Cursor.SW_RESIZE_CURSOR: return SWT.CURSOR_SIZENESW;
            case Cursor.NW_RESIZE_CURSOR:
            case Cursor.SE_RESIZE_CURSOR: return SWT.CURSOR_SIZENWSE;
            default: return SWT.CURSOR_ARROW;
        }
    }

    /** The image a custom cursor was made from, kept in a field of the toolkit's cursor class. */
    static Image customImage(Cursor cursor) {
        for (Class<?> c = cursor.getClass(); c != null && c != Cursor.class; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField("image");
                if (!Image.class.isAssignableFrom(field.getType())) return null;
                field.setAccessible(true);
                return (Image) field.get(cursor);
            } catch (NoSuchFieldException next) {
                // Declared further up, if at all.
            } catch (ReflectiveOperationException | RuntimeException inaccessible) {
                return null;
            }
        }
        return null;
    }

    /** {@code image} at the size the platform draws cursors in, the size its hotspot refers to. */
    static ImageData imageDataOf(Image image) {
        if (image == null) return null;
        int w = image.getWidth(null), h = image.getHeight(null);
        if (w <= 0 || h <= 0) return null;
        try {
            Dimension best = Toolkit.getDefaultToolkit().getBestCursorSize(w, h);
            if (best.width > 0 && best.height > 0 && (best.width != w || best.height != h)) {
                image = image.getScaledInstance(best.width, best.height, Image.SCALE_DEFAULT);
                w = best.width;
                h = best.height;
            }
        } catch (RuntimeException headless) {
            // Kept at its own size.
        }
        return imageDataOf(image, w, h);
    }

    static ImageData imageDataOf(Image image, int w, int h) {
        int[] argb = new int[w * h];
        try {
            PixelGrabber grabber = new PixelGrabber(image.getSource(), 0, 0, w, h, argb, 0, w);
            if (!grabber.grabPixels()) return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        ImageData data = new ImageData(w, h, 32, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        byte[] alpha = new byte[w * h];
        for (int i = 0; i < argb.length; i++) {
            data.setPixel(i % w, i / w, argb[i] & 0xFFFFFF);
            alpha[i] = (byte) (argb[i] >>> 24);
        }
        data.alphaData = alpha;
        return data;
    }

    private static int[] nativeHotspot(Cursor cursor, int width, int height) {
        if (NATIVE_HOTSPOT == null) return null;
        try {
            return (int[]) NATIVE_HOTSPOT.invoke(null, cursor, width, height);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError unavailable) {
            return null;
        }
    }

    private static Method nativeHotspotMethod() {
        try {
            Method of = Class.forName("dev.equo.swt.awt.NativeCursorHotspot")
                    .getDeclaredMethod("of", Cursor.class, int.class, int.class);
            of.setAccessible(true);
            return of;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException notOnThisPlatform) {
            return null;
        }
    }
}
