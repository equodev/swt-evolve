package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplComposite extends ImplScrollable {

    long _imHandle();

    long _socketHandle();

    Layout _layout();

    Control[] _tabList();

    int _layoutCount();

    int _backgroundMode();

    long _fixClipHandle();

    Map<Control, long[]> _fixClipMap();

    Shell _popupChild();

    boolean _noChildDrawing();

    HashMap<Widget, Boolean> _childrenLowered();

    Control[] _getChildren();

    Control[] _getTabList();

    Widget[] computeTabList();

    Composite findDeferredControl();

    Menu[] findMenus(Control control);

    void fixChildren(Shell newShell, Shell oldShell, Decorations newDecorations, Decorations oldDecorations, Menu[] menus);

    void fixStyle();

    void fixZOrder();

    boolean isTabGroup();

    void markLayout(boolean changed, boolean all);

    void moveAbove(long child, long sibling);

    void moveBelow(long child, long sibling);

    long parentingHandle();

    void removeControl(Control control);

    boolean setTabGroupFocus(boolean next);

    boolean setTabItemFocus(boolean next);

    boolean translateMnemonic(Event event, Control control);

    void updateBackgroundMode();

    void updateLayout(boolean all);
}
