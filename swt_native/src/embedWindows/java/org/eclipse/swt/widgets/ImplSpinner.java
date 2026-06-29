package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplSpinner extends ImplComposite {

    long _hwndText();

    long _hwndUpDown();

    boolean _ignoreModify();

    boolean _ignoreCharacter();

    int _pageIncrement();

    int _digits();
}
