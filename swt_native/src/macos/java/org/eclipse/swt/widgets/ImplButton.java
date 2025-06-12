package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplButton extends ImplControl {

    String _text();

    Image _image();

    boolean _grayed();
}
