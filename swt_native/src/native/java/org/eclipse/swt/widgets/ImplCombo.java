package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplCombo extends ImplComposite {

    String _text();

    int _textLimit();

    boolean _receivingFocus();

    boolean _ignoreSetObject();

    boolean _ignoreSelection();

    boolean _listVisible();

    Cursor findCursor();
}
