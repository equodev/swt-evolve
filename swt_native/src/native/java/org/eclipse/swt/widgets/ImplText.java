package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display.*;

public interface ImplText extends ImplScrollable {

    int _textLimit();

    int _tabs();

    char _echoCharacter();

    boolean _doubleClick();

    boolean _receivingFocus();

    char[] _hiddenText();

    String _message();

    long _actionSearch();

    long _actionCancel();

    void updateCursorRects(boolean enabled);
}
