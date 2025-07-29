package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplCombo extends ImplComposite {

    boolean _noSelection();

    boolean _ignoreDefaultSelection();

    boolean _ignoreCharacter();

    boolean _ignoreModify();

    boolean _ignoreResize();

    boolean _lockText();

    int _scrollWidth();

    int _visibleCount();

    long _cbtHook();

    String[] _items();

    int[] _segments();

    int _clearSegmentsCount();

    boolean _stateFlagsUsable();
}
