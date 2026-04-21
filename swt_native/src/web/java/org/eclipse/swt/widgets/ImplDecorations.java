package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplDecorations extends ImplCanvas {

    Image _image();

    Image[] _images();

    Menu _menuBar();

    String _text();

    boolean _minimized();

    boolean _maximized();

    Control _savedFocus();

    Button _defaultButton();

    Control computeTabRoot();

    boolean isTabGroup();

    boolean isTabItem();

    Decorations menuShell();
}
