package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTree extends ImplComposite {

    long _modelHandle();

    long _checkRenderer();

    int _columnCount();

    int _sortDirection();

    int _selectionCountOnPress();

    int _selectionCountOnRelease();

    long _ignoreCell();

    TreeItem[] _items();

    int _nextId();

    TreeColumn[] _columns();

    TreeColumn _sortColumn();

    TreeItem _currentItem();

    boolean _firstCustomDraw();

    boolean _firstCompute();

    boolean _modelChanged();

    boolean _expandAll();

    int _drawState();

    int _drawFlags();

    boolean _isOwnerDrawn();

    boolean _ignoreSize();

    boolean _pixbufSizeSet();

    boolean _hasChildren();

    int _pixbufHeight();

    int _pixbufWidth();

    int _headerHeight();

    boolean _headerVisible();

    TreeItem _topItem();

    double _cachedAdjustment();

    double _currentAdjustment();

    Color _headerBackground();

    Color _headerForeground();

    boolean _boundsChangedSinceLastDraw();

    boolean _wasScrolled();

    boolean _rowActivated();
}
