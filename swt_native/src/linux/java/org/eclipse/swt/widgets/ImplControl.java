package org.eclipse.swt.widgets;

import java.lang.reflect.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplControl extends ImplWidget {

    long _fixedHandle();

    long _firstFixedHandle();

    long _keyController();

    long _redrawWindow();

    long _enableWindow();

    long _provider();

    int _drawCount();

    int _backgroundAlpha();

    long _dragGesture();

    long _zoomGesture();

    long _rotateGesture();

    long _panGesture();

    Composite _parent();

    Cursor _cursor();

    Menu _menu();

    Image _backgroundImage();

    Font _font();

    Region _region();

    long _eventRegion();

    long _regionHandle();

    String _toolTipText();

    Object _layoutData();

    Accessible _accessible();

    Control _labelRelation();

    String _cssBackground();

    String _cssForeground();

    boolean _drawRegion();

    boolean _cachedNoBackground();

    Point _lastInput();

    LinkedList<Event> _dragDetectionQueue();

    boolean _checkScaleFactor();

    boolean _autoScale();

    void fixStyle();

    void fixStyle(long handle);

    long topHandle();

    Widget[] computeTabList();

    Control computeTabRoot();

    void markLayout(boolean changed, boolean all);

    Point getSizeInPixels();

    boolean isActive();

    boolean isDescribedByLabel();

    void addRelation(Control control);

    void removeRelation();

    Control findBackgroundControl();

    Menu[] findMenus(Control control);

    void fixChildren(Shell newShell, Shell oldShell, Decorations newDecorations, Decorations oldDecorations, Menu[] menus);

    Control[] getPath();

    Shell _getShell();

    boolean isTabGroup();

    boolean isTabItem();

    Decorations menuShell();

    void release(boolean destroy);

    void sendFocusEvent(int type);

    void setCursor(long cursor);

    boolean setTabItemFocus(boolean next);

    boolean traverse(int traversal, char character, int keyCode, int keyLocation, int stateMask, boolean doit);

    boolean translateMnemonic(Event event, Control control);

    boolean translateMnemonic(int keyval, long event);

    boolean traverse(Event event);

    void updateBackgroundMode();

    void updateLayout(boolean all);

    Point getWindowOrigin();

    Point getSurfaceOrigin();
}
