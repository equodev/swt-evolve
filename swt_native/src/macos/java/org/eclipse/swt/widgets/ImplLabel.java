package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplLabel extends ImplControl {

    String _text();

    Image _image();

    boolean _isImage();

    void addRelation(Control control);

    void removeRelation();

    boolean setTabItemFocus();
}
