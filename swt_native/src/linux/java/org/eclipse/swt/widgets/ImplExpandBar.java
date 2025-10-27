package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplExpandBar extends ImplComposite {

    ExpandItem[] _items();

    ExpandItem _lastFocus();

    int _itemCount();

    int _spacing();

    long parentingHandle();
}
