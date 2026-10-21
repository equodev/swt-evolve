package dev.equo.swt;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * The composite a Swing mirror is mounted in: the child {@code SWT_AWT.new_Frame} creates inside the
 * application's EMBEDDED composite while swing-evolve's engine owns the JVM's AWT
 * ({@code dev.equo.swt.awt.SwingIslandHost}). {@link Config} routes it to a {@code DartSwingIsland}
 * by its type, so an EMBEDDED composite used for anything else stays a plain composite.
 *
 * <p>It stands for the native window SWT_AWT embeds, which no SWT layout places: whatever bounds,
 * size or location it is given, it takes the composite's client area, read from the implementation
 * because SWT/Swing integration libraries override the composite's {@code getClientArea()}.
 */
public final class SwingIsland extends Composite {

    public SwingIsland(Composite parent, int style) {
        super(parent, style);
    }

    @Override
    protected void checkSubclass() {
        // A subclass outside org.eclipse.swt, sanctioned the SWT way.
    }

    // Straight to the implementation: on some platforms the API setters call one another.
    public void fill() {
        Rectangle area = getParent().getImpl().getClientArea();
        getImpl().setBounds(area.x, area.y, area.width, area.height);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        fill();
    }

    @Override
    public void setBounds(Rectangle rect) {
        fill();
    }

    @Override
    public void setSize(int width, int height) {
        fill();
    }

    @Override
    public void setSize(Point size) {
        fill();
    }

    @Override
    public void setLocation(int x, int y) {
        fill();
    }

    @Override
    public void setLocation(Point location) {
        fill();
    }
}
