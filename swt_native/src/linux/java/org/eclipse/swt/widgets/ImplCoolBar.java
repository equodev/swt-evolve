package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplCoolBar extends ImplComposite {

    CoolItem[][] _items();

    CoolItem[] _originalItems();

    Cursor _hoverCursor();

    Cursor _dragCursor();

    Cursor _cursor();

    CoolItem _dragging();

    int _mouseXOffset();

    int _itemXOffset();

    boolean _isLocked();

    boolean _inDispose();

    void removeControl(Control control);
}
