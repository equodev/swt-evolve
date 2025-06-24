package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplButton extends ImplControl {

    String _text();

    String _message();

    Image _image();

    Image _disabledImage();

    boolean _ignoreMouse();

    boolean _grayed();

    boolean _useDarkModeExplorerTheme();

    boolean isTabItem();
}
