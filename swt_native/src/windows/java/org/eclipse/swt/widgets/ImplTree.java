package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTree extends ImplComposite {

    TreeItem[] _items();

    TreeColumn[] _columns();

    int _columnCount();

    TreeItem _currentItem();

    TreeColumn _sortColumn();

    long _hAnchor();

    long _hInsert();

    long _hSelect();

    int _lastID();

    int _sortDirection();

    boolean _dragStarted();

    boolean _gestureCompleted();

    boolean _insertAfter();

    boolean _shrink();

    boolean _ignoreShrink();

    boolean _ignoreSelect();

    boolean _ignoreExpand();

    boolean _ignoreDeselect();

    boolean _ignoreResize();

    boolean _lockSelection();

    boolean _oldSelected();

    boolean _newSelected();

    boolean _ignoreColumnMove();

    boolean _ignoreColumnResize();

    boolean _linesVisible();

    boolean _customDraw();

    boolean _painted();

    boolean _ignoreItemHeight();

    boolean _ignoreCustomDraw();

    boolean _ignoreDrawForeground();

    boolean _ignoreDrawBackground();

    boolean _ignoreDrawFocus();

    boolean _ignoreDrawSelection();

    boolean _ignoreDrawHot();

    boolean _ignoreFullSelection();

    boolean _explorerTheme();

    boolean _createdAsRTL();

    boolean _headerItemDragging();

    int _scrollWidth();

    int _selectionForeground();

    long _lastTimerID();

    int _lastTimerCount();

    int _headerBackground();

    int _headerForeground();

    int[] _cachedItemOrder();

    long _cachedFirstItem();

    long _cachedIndexItem();

    int _cachedIndex();

    int _cachedItemCount();

    void setCursor();

    long topHandle();
}
