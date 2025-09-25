package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTable extends ImplComposite {

    TableItem[] _items();

    TableColumn[] _columns();

    TableColumn _sortColumn();

    TableItem _currentItem();

    int _columnCount();

    int _itemCount();

    int _lastIndexOf();

    int _sortDirection();

    int _selectedRowIndex();

    boolean _ignoreSelect();

    boolean _fixScrollWidth();

    boolean _drawExpansion();

    boolean _didSelect();

    boolean _preventSelect();

    boolean _dragDetected();

    Rectangle _imageBounds();

    double[] _headerBackground();

    double[] _headerForeground();

    boolean _shouldScroll();

    boolean _keyDown();

    boolean isTransparent();

    void updateCursorRects(boolean enabled);
}
