package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplToolItem extends ImplItem {

    ToolBar _parent();

    Control _control();

    String _toolTipText();

    Image _disabledImage();

    Image _hotImage();

    Image _disabledImage2();

    int _id();

    short _cx();

    int _foreground();

    int _background();

    Widget[] computeTabList();

    boolean isTabGroup();

    boolean setTabItemFocus();
}
