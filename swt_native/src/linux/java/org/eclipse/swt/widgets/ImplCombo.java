package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplCombo extends ImplComposite {

    long _buttonHandle();

    long _entryHandle();

    long _textRenderer();

    long _cellHandle();

    long _popupHandle();

    long _menuHandle();

    long _buttonBoxHandle();

    long _cellBoxHandle();

    long _arrowHandle();

    int _lastEventTime();

    int _visibleCount();

    long _imContext();

    int _fixStart();

    int _fixEnd();

    String[] _items();

    int _indexSelected();

    long _cssProvider();

    boolean _firstDraw();

    boolean _unselected();

    boolean _fitModelToggled();

    long parentingHandle();
}
