package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplTabItem extends ImplItem {

    long _labelHandle();

    long _imageHandle();

    long _pageHandle();

    long _cssProvider();

    Control _control();

    TabFolder _parent();

    String _toolTipText();

    void release(boolean destroy);
}
