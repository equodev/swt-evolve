package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplSpinner extends ImplComposite {

    int _pageIncrement();

    int _digits();

    int _textLimit();

    Cursor findCursor();

    void updateCursorRects(boolean enabled);
}
