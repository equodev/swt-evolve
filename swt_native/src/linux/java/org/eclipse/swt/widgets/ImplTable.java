package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTable extends ImplComposite {

    long _modelHandle();

    long _checkRenderer();

    int _itemCount();

    int _columnCount();

    int _lastIndexOf();

    int _sortDirection();

    int _selectionCountOnPress();

    int _selectionCountOnRelease();

    long _ignoreCell();

    TableItem[] _items();

    TableColumn[] _columns();

    TableItem _currentItem();

    TableColumn _sortColumn();

    boolean _firstCustomDraw();

    boolean _firstCompute();

    int _drawState();

    int _drawFlags();

    Color _headerBackground();

    Color _headerForeground();

    boolean _ownerDraw();

    boolean _ignoreSize();

    boolean _pixbufSizeSet();

    boolean _hasChildren();

    int _maxWidth();

    int _topIndex();

    double _cachedAdjustment();

    double _currentAdjustment();

    int _pixbufHeight();

    int _pixbufWidth();

    int _headerHeight();

    boolean _boundsChangedSinceLastDraw();

    boolean _headerVisible();

    boolean _wasScrolled();

    boolean _rowActivated();
}
