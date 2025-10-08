package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplList extends ImplScrollable {

    long _modelHandle();

    int _topIndex();

    int _selectionCountOnPress();

    int _selectionCountOnRelease();

    double _cachedAdjustment();

    double _currentAdjustment();

    boolean _rowActivated();
}
