package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplShell extends ImplDecorations {

    long _hostWindowClass();

    long _tooltipOwner();

    long _tooltipTag();

    long _tooltipUserData();

    int _glContextCount();

    boolean _opened();

    boolean _moved();

    boolean _resized();

    boolean _fullScreen();

    boolean _center();

    boolean _deferFlushing();

    boolean _scrolling();

    boolean _isPopup();

    Control _lastActive();

    Rectangle _normalBounds();

    boolean _keyInputHappened();

    ToolBar _toolBar();

    MenuItem _escMenuItem();

    void checkOpen();

    Control findBackgroundControl();

    Composite findDeferredControl();

    Cursor findCursor();

    float getThemeAlpha();

    boolean hasRegion();

    boolean isDrawing();

    boolean isTransparent();

    void setScrolling();

    void updateCursorRects(boolean enabled);

    void updateOpaque();
}
