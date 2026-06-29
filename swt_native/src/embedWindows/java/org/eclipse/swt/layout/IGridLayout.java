package org.eclipse.swt.layout;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IGridLayout extends ILayout {

    Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache);

    boolean flushCache(Control control);

    void layout(Composite composite, boolean flushCache);

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the layout
     */
    String toString();

    GridLayout getApi();
}
