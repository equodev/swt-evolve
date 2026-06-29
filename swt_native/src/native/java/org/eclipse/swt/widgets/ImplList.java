package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplList extends ImplScrollable {

    String[] _items();

    int _itemCount();

    boolean _ignoreSelect();

    boolean _didSelect();

    boolean _rowsChanged();

    boolean _mouseIsDown();
}
