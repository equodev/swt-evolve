package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplCBanner extends ImplComposite {

    Control _left();

    Control _right();

    Control _bottom();

    boolean _simple();

    int[] _curve();

    int _curveStart();

    Rectangle _curveRect();

    int _curve_width();

    int _curve_indent();

    int _rightWidth();

    int _rightMinWidth();

    int _rightMinHeight();

    Cursor _resizeCursor();

    boolean _dragging();

    int _rightDragDisplacement();

    Listener _listener();
}
