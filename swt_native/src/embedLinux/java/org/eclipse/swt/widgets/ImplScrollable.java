package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplScrollable extends ImplControl {

    ScrollBar _horizontalBar();

    ScrollBar _verticalBar();

    void updateBackgroundMode();

    long topHandle();
}
