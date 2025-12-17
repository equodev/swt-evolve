package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTabFolder extends ImplComposite {

    TabItem[] _items();

    Control[] _getChildren();

    void removeControl(Control control);
}
