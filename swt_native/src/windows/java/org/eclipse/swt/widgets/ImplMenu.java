package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplMenu extends ImplWidget {

    int _x();

    int _y();

    long _hBrush();

    int _foreground();

    int _background();

    Image _backgroundImage();

    boolean _hasLocation();

    MenuItem _cascade();

    Decorations _parent();

    MenuItem _selectedMenuItem();

    void _setVisible(boolean visible);
}
