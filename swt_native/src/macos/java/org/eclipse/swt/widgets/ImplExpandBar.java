package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplExpandBar extends ImplComposite {

    ExpandItem[] _items();

    int _itemCount();

    ExpandItem _focusItem();

    int _spacing();

    int _yCurrentScroll();

    Font _font();

    Listener _listener();

    boolean _inDispose();
}
