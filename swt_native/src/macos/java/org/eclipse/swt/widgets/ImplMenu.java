package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplMenu extends ImplWidget {

    int _x();

    int _y();

    int _itemCount();

    boolean _hasLocation();

    boolean _visible();

    MenuItem[] _items();

    MenuItem _cascade();

    MenuItem _defaultItem();

    Decorations _parent();

    void _setVisible(boolean visible);
}
