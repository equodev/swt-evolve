package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTracker extends ImplWidget {

    Control _parent();

    boolean _tracking();

    boolean _cancelled();

    boolean _stippled();

    Cursor _clientCursor();

    Cursor _resizeCursor();

    Rectangle[] _rectangles();

    Rectangle[] _proportions();

    Rectangle _bounds();

    int _cursorOrientation();

    boolean _inEvent();

    int _oldX();

    int _oldY();
}
