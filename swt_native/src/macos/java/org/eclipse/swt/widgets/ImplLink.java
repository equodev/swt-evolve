package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display.*;

public interface ImplLink extends ImplControl {

    String _text();

    Point[] _offsets();

    String[] _ids();

    int[] _mnemonics();

    double[] _linkForeground();

    int _focusIndex();

    boolean _ignoreNextMouseUp();

    APPEARANCE _lastAppAppearance();

    Cursor findCursor();

    void sendFocusEvent(int type);

    void updateCursorRects(boolean enabled);
}
