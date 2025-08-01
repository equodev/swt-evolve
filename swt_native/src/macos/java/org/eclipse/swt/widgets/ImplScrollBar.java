package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplScrollBar extends ImplWidget {

    Scrollable _parent();

    int _minimum();

    int _maximum();

    int _thumb();

    int _increment();

    int _pageIncrement();

    long _actionSelector();

    boolean isDrawing();
}
