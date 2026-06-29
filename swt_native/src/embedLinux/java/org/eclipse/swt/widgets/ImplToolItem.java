package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplToolItem extends ImplItem {

    long _arrowHandle();

    long _labelHandle();

    long _imageHandle();

    long _eventHandle();

    long _proxyMenuItem();

    long _provider();

    long _boxHandle();

    long _groupHandle();

    ToolBar _parent();

    Control _control();

    Image _hotImage();

    Image _disabledImage();

    Image _defaultDisableImage();

    Color _background();

    Color _foreground();

    String _toolTipText();

    boolean _drawHotImage();

    boolean _mapHooked();

    boolean _enabled();

    Image _currentImage();

    Widget[] computeTabList();

    boolean isTabGroup();

    boolean setTabItemFocus(boolean next);
}
