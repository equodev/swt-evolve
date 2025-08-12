package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplCanvas extends ImplComposite {

    Caret _caret();

    IME _ime();

    boolean _blink();

    boolean _drawFlag();
}
