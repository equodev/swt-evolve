package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ISashFormLayout extends ILayout, ImplSashFormLayout {

    Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache);

    boolean flushCache(Control control);

    void layout(Composite composite, boolean flushCache);

    SashFormLayout getApi();
}
