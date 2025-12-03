package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplScrolledComposite extends ImplComposite {

    Control _content();

    Listener _contentListener();

    Listener _filter();

    int _minHeight();

    int _minWidth();

    boolean _expandHorizontal();

    boolean _expandVertical();

    boolean _alwaysShowScroll();

    boolean _showFocusedControl();

    boolean _showNextFocusedControl();

    boolean needHScroll(Rectangle contentRect, boolean vVisible);

    boolean needVScroll(Rectangle contentRect, boolean hVisible);
}
