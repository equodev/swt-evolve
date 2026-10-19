package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Rectangle;

/** What a control's Paint scheduling remembers between a redraw() and the Paint that answers it. */
final class PaintState {

    /** A Paint is on its way. Not drawCount, which is SWT's setRedraw counter. */
    boolean queued;

    /**
     * The client still shows a frame a Paint listener drew. Native SWT repaints on every redraw(),
     * listener or not; here only a Paint cycle replaces that frame.
     */
    boolean showsPaintedFrame;

    private boolean damaged;
    /** The area owed while {@link #damaged}; null is the whole client area. */
    private Rectangle damage;

    void damageAll() {
        damaged = true;
        damage = null;
    }

    /** Adds [area] to what the next Paint owes, the way SWT merges paint requests. */
    void damage(Rectangle area) {
        if (!damaged) {
            damaged = true;
            damage = area;
        } else if (damage != null) {
            damage.add(area);
        }
    }

    /** The area to paint, clamped to [bounds], or null when nothing is left to paint. Clears it. */
    Rectangle takeDamage(Rectangle bounds) {
        if (!damaged) return bounds;
        Rectangle area = damage;
        damaged = false;
        damage = null;
        if (area == null) return bounds;
        area.intersect(bounds);
        return area.isEmpty() ? null : area;
    }
}
