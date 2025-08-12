package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplToolBar extends ImplComposite {

    ToolItem _currentFocusItem();

    ToolItem[] _tabItemList();

    boolean _hasChildFocus();

    String _cssBackground();

    String _cssForeground();

    Widget[] computeTabList();

    void removeControl(Control control);
}
