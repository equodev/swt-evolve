package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;

public interface ImplLabel extends ImplControl {

    long _frameHandle();

    long _labelHandle();

    long _imageHandle();

    long _boxHandle();

    Image _image();

    String _text();

    void addRelation(Control control);

    boolean isDescribedByLabel();
}
