package dev.equo.swt.awt;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * Paints an off-screen OpenGL canvas again only where it was damaged since its last paint.
 *
 * <p>Painting such a canvas renders its whole scene again, and a render wipes whatever it drew
 * into its front buffer outside a paint, such as an XOR tracking cursor. On screen the canvas is
 * only repainted when it asks for it or something covers it, so the host keeps each canvas's last
 * image and composites that until the canvas posts a repaint of its own (only the area it names is
 * painted again) or the embedding is forced to repaint after input or a resize (all of it is).
 * Event dispatch thread only.
 */
final class CanvasDamage {

    private static final class Entry {
        BufferedImage image;
        Rectangle damage;
        boolean whole = true;
    }

    private static final Map<Component, Entry> ENTRIES = new WeakHashMap<>();

    private CanvasDamage() {}

    /**
     * {@code source} asked for {@code area} (in its own coordinates; null for all of it) to be
     * repainted: that area of it, if it is a canvas, and every canvas under it, in full.
     */
    static void repaintRequested(Component source, Rectangle area) {
        Entry entry = ENTRIES.get(source);
        if (entry != null && !entry.whole) {
            if (area == null) {
                entry.whole = true;
            } else {
                entry.damage = entry.damage == null ? new Rectangle(area) : entry.damage.union(area);
            }
        }
        if (source instanceof Container) damageUnder((Container) source);
    }

    /** Marks every canvas at or under {@code root} to be painted again in full. */
    static void damageAll(Container root) {
        Entry entry = ENTRIES.get(root);
        if (entry != null) entry.whole = true;
        damageUnder(root);
    }

    private static void damageUnder(Container root) {
        for (Map.Entry<Component, Entry> e : ENTRIES.entrySet()) {
            if (root.isAncestorOf(e.getKey())) e.getValue().whole = true;
        }
    }

    /**
     * {@code canvas} as an {@code sw}x{@code sh} image at the given scale: the last one when nothing
     * was damaged, else that image with {@code painter} run again over the damage, or a fresh full
     * paint. Null when {@code painter} fails; the canvas is then painted in full next time.
     */
    static BufferedImage image(Component canvas, int sw, int sh, double scaleX, double scaleY,
            Consumer<Graphics2D> painter) {
        Entry entry = ENTRIES.computeIfAbsent(canvas, c -> new Entry());
        BufferedImage last = entry.image;
        if (last == null || last.getWidth() != sw || last.getHeight() != sh) entry.whole = true;
        if (!entry.whole && entry.damage == null) return last;

        BufferedImage image = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            if (!entry.whole) g.drawImage(last, 0, 0, null);
            g.scale(scaleX, scaleY);
            if (!entry.whole) {
                g.clip(entry.damage);
                g.setComposite(AlphaComposite.Clear);
                g.fill(entry.damage);
                g.setComposite(AlphaComposite.SrcOver);
            }
            painter.accept(g);
        } catch (Throwable t) {
            entry.whole = true;
            return null;
        } finally {
            g.dispose();
        }
        entry.image = image;
        entry.damage = null;
        entry.whole = false;
        return image;
    }
}
