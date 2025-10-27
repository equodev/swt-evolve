package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplExpandItem extends ImplItem {

    ExpandBar _parent();

    Control _control();

    long _clientHandle();

    long _boxHandle();

    long _labelHandle();

    long _imageHandle();

    int _width();

    int _height();

    void release(boolean destroy);
}
