package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplCaret extends ImplWidget {

    Canvas _parent();

    int _x();

    int _y();

    int _width();

    int _height();

    boolean _isVisible();

    boolean _isShowing();

    int _blinkRate();

    Image _image();

    Font _font();

    boolean blinkCaret();
}
