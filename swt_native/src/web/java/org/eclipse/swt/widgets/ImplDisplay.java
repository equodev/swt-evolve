package org.eclipse.swt.widgets;

import java.lang.Runtime.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplDisplay extends ImplDevice {

    Event[] _eventQueue();

    EventTable _eventTable();

    void _eventTable(EventTable eventTable);

    EventTable _filterTable();

    boolean _disposing();

    int _sendEventCount();

    double _rotation();

    double _magnification();

    boolean _gestureActive();

    int _touchCounter();

    long _primaryIdentifier();

    TouchSource[] _touchSources();

    Synchronizer _synchronizer();

    Consumer<RuntimeException> _runtimeExceptionHandler();

    Consumer<Error> _errorHandler();

    Thread _thread();

    boolean _allowTimers();

    boolean _runAsyncMessages();

    GCData[] _contexts();

    Caret _currentCaret();

    boolean _sendEvent();

    int _clickCountButton();

    int _clickCount();

    int _blinkTime();

    Control _currentControl();

    Control _trackingControl();

    Control _tooltipControl();

    Control _ignoreFocusControl();

    Widget _tooltipTarget();

    boolean _smallFonts();

    boolean _useNativeItemHeight();

    Shell[] _modalShells();

    Dialog _modalDialog();

    Menu _menuBar();

    Menu[] _menus();

    Menu[] _popups();

    boolean _escAsAcceleratorPresent();

    boolean _isEmbedded();

    Control _focusControl();

    Control _currentFocusControl();

    int _focusEvent();

    int _poolCount();

    int _loopCount();

    int[] _screenID();

    boolean _lockCursor();

    Combo _currentCombo();

    Runnable[] _disposeList();

    Composite[] _layoutDeferred();

    int _layoutDeferredCount();

    Tray _tray();

    TrayItem _currentTrayItem();

    Menu _trayItemMenu();

    Menu _appMenuBar();

    Menu _appMenu();

    TaskBar _taskBar();

    Image _errorImage();

    Image _infoImage();

    Image _warningImage();

    Cursor[] _cursors();

    double[][] _colors();

    double[] _alternateSelectedControlTextColor();

    double[] _selectedControlTextColor();

    Widget[] _skinList();

    int _skinCount();

    Runnable[] _timerList();

    boolean _runSettings();

    Object _data();

    String[] _keys();

    Object[] _values();

    Runnable _caretTimer();

    int getCaretBlinkTime();

    void setCurrentCaret(Caret caret);
}
