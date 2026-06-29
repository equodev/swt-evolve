package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplCoolBar extends ImplComposite {

    CoolItem[] _items();

    CoolItem[] _originalItems();

    boolean _locked();

    boolean _ignoreResize();

    Control findThemeControl();

    void removeControl(Control control);
}
