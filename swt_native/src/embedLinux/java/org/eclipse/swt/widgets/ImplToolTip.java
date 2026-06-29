package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplToolTip extends ImplWidget {

    Shell _parent();

    String _text();

    String _message();

    TrayItem _item();

    int _x();

    int _y();

    int _timerId();

    long _layoutText();

    long _layoutMessage();

    long _provider();

    int[] _borderPolygon();

    boolean _spikeAbove();

    boolean _autohide();
}
