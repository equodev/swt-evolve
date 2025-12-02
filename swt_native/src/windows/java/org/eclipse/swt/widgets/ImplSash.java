package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplSash extends ImplControl {

    boolean _dragging();

    int _startX();

    int _startY();

    int _lastX();

    int _lastY();
}
