package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplButton extends ImplControl {

    long _boxHandle();

    long _labelHandle();

    long _imageHandle();

    long _arrowHandle();

    long _groupHandle();

    boolean _selected();

    boolean _grayed();

    boolean _toggleButtonTheming();

    Image _image();

    String _text();

    Image _defaultDisableImage();

    boolean _enabled();

    boolean isDescribedByLabel();
}
