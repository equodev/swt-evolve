package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplCaret extends ImplWidget {

    Canvas _parent();

    int _x();

    int _y();

    int _width();

    int _height();

    boolean _moved();

    boolean _resized();

    boolean _isVisible();

    Image _image();

    Font _font();
}
