package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTreeColumn extends ImplItem {

    long _headerButtonCSSProvider();

    long _labelHandle();

    long _imageHandle();

    long _buttonHandle();

    Tree _parent();

    int _modelIndex();

    int _lastTime();

    int _lastX();

    int _lastWidth();

    boolean _customDraw();

    String _toolTipText();
}
