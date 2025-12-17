package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplTabItem extends ImplItem {

    TabFolder _parent();

    Control _control();

    String _toolTipText();
}
