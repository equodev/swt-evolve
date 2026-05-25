package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;

public interface ImplToolBar extends ImplComposite {

    int _itemCount();

    ToolItem[] _items();

    ToolItem _lastFocus();

    void removeControl(Control control);
}
