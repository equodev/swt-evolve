package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplText extends ImplScrollable {

    long _bufferHandle();

    long _imContext();

    int _tabs();

    int _lastEventTime();

    int _fixStart();

    int _fixEnd();

    boolean _doubleClick();

    String _message();

    long _textHandle();

    int[] _segments();

    long _indexMark();

    double _cachedAdjustment();

    double _currentAdjustment();

    void setCursor(long cursor);
}
