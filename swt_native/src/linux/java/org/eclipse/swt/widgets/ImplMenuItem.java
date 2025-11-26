package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu.*;

public interface ImplMenuItem extends ImplItem {

    Menu _parent();

    Menu _menu();

    long _groupHandle();

    long _labelHandle();

    long _imageHandle();

    long _boxHandle();

    int _accelerator();

    int _userId();

    String _toolTipText();

    Image _defaultDisableImage();

    boolean _enabled();

    long _modelHandle();

    long _actionHandle();

    long _shortcutHandle();

    String _actionName();
}
