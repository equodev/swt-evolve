package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTabFolder extends ImplComposite {

    TabItem[] _items();

    int _itemCount();

    boolean _ignoreSelect();

    float getThemeAlpha();

    boolean isTransparent();

    void removeControl(Control control);
}
