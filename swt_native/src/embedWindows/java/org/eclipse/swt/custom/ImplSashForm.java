package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplSashForm extends ImplComposite {

    int _sashStyle();

    Sash[] _sashes();

    Control[] _controls();

    Control _maxControl();

    Listener _sashListener();

    Sash createSash();

    Control[] getControls(boolean onlyVisible);
}
