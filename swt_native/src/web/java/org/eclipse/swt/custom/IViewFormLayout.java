package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IViewFormLayout extends ILayout, ImplViewFormLayout {

    Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache);

    boolean flushCache(Control control);

    void layout(Composite composite, boolean flushCache);

    ViewFormLayout getApi();
}
