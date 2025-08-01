package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
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

    void markLayout(boolean changed, boolean all);

    void removeControl(Control control);

    boolean setTabGroupFocus();

    boolean translateMnemonic(Event event, Control control);

    void updateBackgroundMode();

    void updateLayout(boolean all);

    void updateLayout(boolean resize, boolean all);
}
