package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplSash extends ImplControl {

    Cursor _sizeCursor();

    boolean _dragging();

    int _lastX();

    int _lastY();

    int _startX();

    int _startY();

    Cursor findCursor();
}
