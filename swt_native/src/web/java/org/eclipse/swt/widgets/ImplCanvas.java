package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;

public interface ImplCanvas extends ImplComposite {

    Caret _caret();

    IME _ime();

    void sendFocusEvent(int type);

    void resetVisibleRegion();
}
