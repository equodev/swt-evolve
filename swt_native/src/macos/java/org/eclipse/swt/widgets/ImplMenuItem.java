package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplMenuItem extends ImplItem {

    Menu _parent();

    Menu _menu();

    int _accelerator();

    long _nsItemAction();

    String _toolTipText();
}
