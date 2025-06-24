package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplControl extends ImplWidget {

    Composite _parent();

    Cursor _cursor();

    Menu _menu();

    Menu _activeMenu();

    String _toolTipText();

    Object _layoutData();

    Accessible _accessible();

    Image _backgroundImage();

    Region _region();

    Font _font();

    int _drawCount();

    int _backgroundAlpha();

    Widget[] computeTabList();

    Control findBackgroundControl();

    Cursor findCursor();

    Menu[] findMenus(Control control);

    void fixChildren(Shell newShell, Shell oldShell, Decorations newDecorations, Decorations oldDecorations, Menu[] menus);

    Control[] getPath();

    boolean isActive();

    boolean isTabItem();

    void markLayout(boolean changed, boolean all);

    Decorations menuShell();

    boolean sendFocusEvent(int type);

    void updateBackgroundMode();

    void updateLayout(boolean resize, boolean all);
}
