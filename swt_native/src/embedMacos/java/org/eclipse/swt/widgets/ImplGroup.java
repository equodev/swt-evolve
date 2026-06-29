package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplGroup extends ImplComposite {

    String _text();

    boolean _ignoreResize();

    int _hMargin();

    int _vMargin();

    boolean isTransparent();

    float getThemeAlpha();
}
