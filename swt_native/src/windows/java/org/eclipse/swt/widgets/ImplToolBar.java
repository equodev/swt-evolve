package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplToolBar extends ImplComposite {

    int _lastFocusId();

    int _lastArrowId();

    int _lastHotId();

    int __width();

    int __height();

    int __count();

    int __wHint();

    int __hHint();

    long _currentToolItemToolTip();

    ToolItem[] _items();

    ToolItem[] _tabItemList();

    boolean _ignoreResize();

    boolean _ignoreMouse();

    Widget[] computeTabList();

    void removeControl(Control control);

    boolean setTabItemFocus();
}
