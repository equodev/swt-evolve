package dev.equo.swt;

import org.eclipse.swt.widgets.Composite;

/**
 * The composite a Swing mirror is mounted in: the child {@code SWT_AWT.new_Frame} creates inside the
 * application's EMBEDDED composite while swing-evolve's engine owns the JVM's AWT
 * ({@code dev.equo.swt.awt.SwingIslandHost}). {@link Config} routes it to a {@code DartSwingIsland}
 * by its type, so an EMBEDDED composite used for anything else stays a plain composite.
 */
public final class SwingIsland extends Composite {

    public SwingIsland(Composite parent, int style) {
        super(parent, style);
    }

    @Override
    protected void checkSubclass() {
        // A subclass outside org.eclipse.swt, sanctioned the SWT way.
    }
}
