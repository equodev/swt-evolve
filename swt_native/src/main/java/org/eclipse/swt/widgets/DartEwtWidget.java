package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

/**
 * Dart impl for an EWT region. Subclass of {@link DartComposite}; its main job is to
 * make the widget serialize as an "EwtWidget" node (the type string is derived from
 * this class name), which the Dart side maps to the EWT region hook. Evolve stays
 * EWT-agnostic: no reference to EWT here.
 *
 * It also carries a preferred size. An EWT region hosts a Flutter subtree whose
 * intrinsic size lives on the Flutter side and is not knowable in Java at layout time,
 * so a plain composite would collapse to the default 64x64 and any SWT layout would
 * mis-size it. The app declares the region's natural size via {@link #setPreferredSize},
 * and {@link #computeSize} reports it so the region participates in SWT layout normally.
 */
public class DartEwtWidget extends DartComposite {

    private int preferredWidth = SWT.DEFAULT;
    private int preferredHeight = SWT.DEFAULT;

    public DartEwtWidget(Composite parent, int style, Composite composite) {
        super(parent, style, composite);
    }

    /**
     * Declares the region's natural size for SWT layout. Either dimension may be
     * {@link SWT#DEFAULT} to leave it to the default computation.
     */
    public void setPreferredSize(int width, int height) {
        this.preferredWidth = width;
        this.preferredHeight = height;
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        // An explicit hint always wins; then the app-declared preferred size; then the
        // composite default (super, which for a childless region is DEFAULT_WIDTH/HEIGHT).
        Point base = super.computeSize(wHint, hHint, changed);
        int w = wHint != SWT.DEFAULT ? wHint : (preferredWidth != SWT.DEFAULT ? preferredWidth : base.x);
        int h = hHint != SWT.DEFAULT ? hHint : (preferredHeight != SWT.DEFAULT ? preferredHeight : base.y);
        return new Point(w, h);
    }
}
