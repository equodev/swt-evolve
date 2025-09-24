package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTableColumn extends ImplItem {

    long _headerButtonCSSProvider();

    long _labelHandle();

    long _imageHandle();

    long _buttonHandle();

    Table _parent();

    int _modelIndex();

    int _lastButton();

    int _lastTime();

    int _lastX();

    int _lastWidth();

    boolean _customDraw();

    boolean _useFixedWidth();

    String _toolTipText();
}
