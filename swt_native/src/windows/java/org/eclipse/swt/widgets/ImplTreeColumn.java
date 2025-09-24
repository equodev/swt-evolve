package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTreeColumn extends ImplItem {

    Tree _parent();

    boolean _resizable();

    boolean _moveable();

    String _toolTipText();

    int _id();
}
