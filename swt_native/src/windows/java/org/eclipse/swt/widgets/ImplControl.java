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

    int _foreground();

    int _background();

    int _backgroundAlpha();

    Widget[] computeTabList();

    Control findBackgroundControl();

    long findBrush(long value, int lbStyle);

    Cursor findCursor();

    Control findThemeControl();

    Menu[] findMenus(Control control);

    void fixChildren(Shell newShell, Shell oldShell, Decorations newDecorations, Decorations oldDecorations, Menu[] menus);

    int getBackgroundPixel();

    Control[] getPath();

    Point getSizeInPixels();

    boolean isActive();

    boolean isTabGroup();

    boolean isTabItem();

    void markLayout(boolean changed, boolean all);

    Decorations menuShell();

    boolean sendFocusEvent(int type);

    void setCursor();

    boolean setRadioFocus(boolean tabbing);

    boolean setTabItemFocus();

    boolean translateMnemonic(Event event, Control control);

    boolean traverse(Event event);

    boolean traverse(int traversal, char character, int keyCode, int keyLocation, int stateMask, boolean doit);

    void updateBackgroundMode();

    void updateLayout(boolean resize, boolean all);
}
