package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplSlider extends ImplControl {

    int _increment();

    int _pageIncrement();

    boolean _ignoreFocus();
}
