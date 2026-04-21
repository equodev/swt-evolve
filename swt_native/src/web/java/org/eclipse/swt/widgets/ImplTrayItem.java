package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplTrayItem extends ImplItem {

    Tray _parent();

    ToolTip _toolTip();

    String _toolTipText();

    boolean _visible();

    boolean _highlight();

    Image _highlightImage();
}
