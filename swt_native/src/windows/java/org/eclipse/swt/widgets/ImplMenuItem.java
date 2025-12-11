package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplMenuItem extends ImplItem {

    Menu _parent();

    Menu _menu();

    long _hBitmap();

    Image _imageSelected();

    long _hBitmapSelected();

    int _id();

    int _accelerator();

    int _userId();

    ToolTip _itemToolTip();
}
