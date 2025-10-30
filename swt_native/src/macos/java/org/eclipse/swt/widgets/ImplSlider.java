package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplSlider extends ImplControl {

    boolean _dragging();

    int _minimum();

    int _maximum();

    int _thumb();

    int _increment();

    int _pageIncrement();
}
