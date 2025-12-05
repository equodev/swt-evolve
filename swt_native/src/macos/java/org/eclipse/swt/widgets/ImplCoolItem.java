package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplCoolItem extends ImplItem {

    Control _control();

    CoolBar _parent();

    boolean _ideal();

    int _preferredWidth();

    int _preferredHeight();

    int _minimumWidth();

    int _minimumHeight();

    int _requestedWidth();

    Rectangle _itemBounds();

    ToolBar _chevron();

    boolean _wrap();

    Image _arrowImage();
}
