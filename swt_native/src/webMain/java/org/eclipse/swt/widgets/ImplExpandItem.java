package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplExpandItem extends ImplItem {

    ExpandBar _parent();

    Control _control();

    boolean _expanded();

    int _x();

    int _y();

    int _width();

    int _height();

    int _imageHeight();

    int _imageWidth();
}
