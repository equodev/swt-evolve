package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTree extends ImplComposite {

    TreeItem[] _items();

    int _itemCount();

    TreeColumn[] _columns();

    TreeColumn _sortColumn();

    int _columnCount();

    int _sortDirection();

    int _selectedRowIndex();

    boolean _ignoreExpand();

    boolean _ignoreSelect();

    boolean _ignoreRedraw();

    boolean _reloadPending();

    boolean _drawExpansion();

    boolean _didSelect();

    boolean _preventSelect();

    boolean _dragDetected();

    Rectangle _imageBounds();

    TreeItem _insertItem();

    boolean _insertBefore();

    double[] _headerBackground();

    double[] _headerForeground();

    boolean _shouldExpand();

    boolean _shouldScroll();

    boolean _keyDown();

    boolean isTransparent();

    void updateCursorRects(boolean enabled);
}
