package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplSpinner extends ImplComposite {

    int _lastEventTime();

    long _imContext();

    long _entryHandle();

    int _fixStart();

    int _fixEnd();

    double _climbRate();

    void setCursor(long cursor);
}
