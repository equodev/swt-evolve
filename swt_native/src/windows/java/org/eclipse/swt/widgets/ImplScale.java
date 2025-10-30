package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplScale extends ImplControl {

    boolean _ignoreResize();

    boolean _ignoreSelection();

    boolean _createdAsRTL();
}
