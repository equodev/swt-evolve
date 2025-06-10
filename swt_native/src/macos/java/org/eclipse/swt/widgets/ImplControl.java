package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplControl extends ImplWidget {

    Composite _parent();

    String _toolTipText();

    Object _layoutData();

    int _drawCount();

    int _backgroundAlpha();

    Menu _menu();

    Image _backgroundImage();

    Font _font();

    Cursor _cursor();

    Region _region();

    long _visibleRgn();

    Accessible _accessible();

    boolean _inCacheDisplayInRect();

    boolean _touchEnabled();

    void addRelation(Control control);

    Widget[] computeTabList();

    Cursor findCursor();

    Menu[] findMenus(Control control);

    Control[] getPath();

    void invalidateChildrenVisibleRegion();

    boolean isDrawing();

    boolean isTabItem();

    boolean isTransparent();

    Decorations menuShell();

    void release(boolean destroy);

    void removeRelation();

    void resetVisibleRegion();
}
