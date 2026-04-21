package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTableColumn extends ImplItem {

    Table _parent();

    String _toolTipText();

    String _displayText();

    boolean _movable();
}
