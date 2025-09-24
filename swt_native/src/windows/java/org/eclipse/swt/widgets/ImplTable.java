package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTable extends ImplComposite {

    TableItem[] _items();

    TableColumn[] _columns();

    int _columnCount();

    TableItem _currentItem();

    TableColumn _sortColumn();

    boolean[] _columnVisible();

    long _headerToolTipHandle();

    long _hwndHeader();

    long _itemToolTipHandle();

    boolean _ignoreCustomDraw();

    boolean _ignoreDrawForeground();

    boolean _ignoreDrawBackground();

    boolean _ignoreDrawFocus();

    boolean _ignoreDrawSelection();

    boolean _ignoreDrawHot();

    boolean _customDraw();

    boolean _dragStarted();

    boolean _explorerTheme();

    boolean _firstColumnImage();

    boolean _fixScrollWidth();

    boolean _tipRequested();

    boolean _wasSelected();

    boolean _wasResized();

    boolean _painted();

    boolean _ignoreActivate();

    boolean _ignoreSelect();

    boolean _ignoreShrink();

    boolean _ignoreResize();

    boolean _ignoreColumnMove();

    boolean _ignoreColumnResize();

    boolean _fullRowSelect();

    boolean _settingItemHeight();

    boolean _headerItemDragging();

    int _itemHeight();

    int _lastIndexOf();

    int _lastWidth();

    int _sortDirection();

    int _resizeCount();

    int _selectionForeground();

    int _hotIndex();

    int _headerBackground();

    int _headerForeground();
}
