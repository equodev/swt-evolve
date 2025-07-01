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

    Control[] _getTabList();

    Widget[] computeTabList();

    Composite findDeferredControl();

    Menu[] findMenus(Control control);

    void fixChildren(Shell newShell, Shell oldShell, Decorations newDecorations, Decorations oldDecorations, Menu[] menus);

    void invalidateChildrenVisibleRegion();

    boolean isTabGroup();

    void markLayout(boolean changed, boolean all);

    void removeControl(Control control);

    boolean setTabGroupFocus();

    void updateBackgroundMode();

    void updateCursorRects(boolean enabled);

    void updateLayout(boolean all);
}
