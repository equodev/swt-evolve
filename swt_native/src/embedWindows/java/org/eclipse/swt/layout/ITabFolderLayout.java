package org.eclipse.swt.layout;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ITabFolderLayout extends ILayout {

    Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache);

    void layout(Composite composite, boolean flushCache);

    TabFolderLayout getApi();
}
