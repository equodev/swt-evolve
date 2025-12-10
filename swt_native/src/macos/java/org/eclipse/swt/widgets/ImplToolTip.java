package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplToolTip extends ImplWidget {

    Shell _parent();

    Shell _tip();

    TrayItem _item();

    int _x();

    int _y();

    int[] _borderPolygon();

    boolean _spikeAbove();

    boolean _autohide();

    Listener _listener();

    Listener _parentListener();

    TextLayout _layoutText();

    TextLayout _layoutMessage();

    Region _region();

    Font _boldFont();

    Runnable _runnable();
}
