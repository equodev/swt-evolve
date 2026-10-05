package org.eclipse.swt.widgets;

/**
 * The implementation of a {@code dev.equo.swt.SwingIsland}, the composite {@code SWT_AWT.new_Frame}
 * creates while swing-evolve's engine owns the JVM's AWT. It has no behaviour of its own: the name
 * is what matters. The client maps a "SwingIsland" node to the region that mounts a {@code SwingMirror}
 * for the frame's window, and {@code dev.equo.swt.awt.SwingIslandHost} reaches that region under
 * the same name.
 */
public class DartSwingIsland extends DartComposite {

    public DartSwingIsland(Composite parent, int style, Composite composite) {
        super(parent, style, composite);
    }
}
