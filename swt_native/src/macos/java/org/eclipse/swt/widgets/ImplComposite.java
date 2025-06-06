package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;

public interface ImplComposite extends ImplScrollable {

    Layout _layout();

    Control[] _tabList();

    int _layoutCount();

    int _backgroundMode();

    Control[] _getChildren();
}
