package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplCTabFolder extends ImplComposite {

    boolean _onBottom();

    boolean _single();

    boolean _simple();

    int _fixedTabHeight();

    int _tabHeight();

    int _minChars();

    boolean _borderVisible();

    CTabFolderRenderer _renderer();

    CTabItem[] _items();

    int _firstIndex();

    int _selectedIndex();

    int[] _priority();

    boolean _mru();

    Listener _listener();

    boolean _ignoreTraverse();

    boolean _useDefaultRenderer();

    CTabFolder2Listener[] _folderListeners();

    CTabFolderListener[] _tabListeners();

    Image _selectionBgImage();

    Color[] _selectionGradientColors();

    int[] _selectionGradientPercents();

    boolean _selectionGradientVertical();

    Color _selectionForeground();

    Color _selectionBackground();

    int _selectionHighlightBarThickness();

    Color[] _gradientColors();

    int[] _gradientPercents();

    boolean _gradientVertical();

    boolean _showUnselectedImage();

    boolean _showSelectedImage();

    boolean _showClose();

    boolean _showUnselectedClose();

    boolean _showMin();

    boolean _minimized();

    boolean _showMax();

    boolean _maximized();

    ToolBar _minMaxTb();

    ToolItem _maxItem();

    ToolItem _minItem();

    Image _maxImage();

    Image _minImage();

    boolean _hoverTb();

    Rectangle _hoverRect();

    boolean _hovering();

    boolean _hoverTimerRunning();

    boolean _highlight();

    boolean _highlightEnabled();

    boolean _showChevron();

    Menu _showMenu();

    ToolBar _chevronTb();

    ToolItem _chevronItem();

    int _chevronCount();

    boolean _chevronVisible();

    Image _chevronImage();

    Control _topRight();

    int _topRightAlignment();

    boolean _ignoreResize();

    Control[] _controls();

    int[] _controlAlignments();

    Rectangle[] _controlRects();

    Rectangle[] _bkImageBounds();

    Image[] _controlBkImages();

    int _updateFlags();

    Runnable _updateRun();

    boolean _inDispose();

    Point _oldSize();

    Font _oldFont();
}
