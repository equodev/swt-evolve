package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplToolTip extends ImplWidget {

    Shell _parent();

    TrayItem _item();

    String _text();

    String _message();

    int _id();

    int _x();

    int _y();

    boolean _autoHide();

    boolean _hasLocation();

    boolean _visible();
}
