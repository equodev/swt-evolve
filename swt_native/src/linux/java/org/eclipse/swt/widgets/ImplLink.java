package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplLink extends ImplControl {

    String _text();

    TextLayout _layout();

    Color _linkColor();

    Color _disabledColor();

    Point[] _offsets();

    Point _selection();

    String[] _ids();

    int[] _mnemonics();

    int _focusIndex();

    void fixStyle();
}
