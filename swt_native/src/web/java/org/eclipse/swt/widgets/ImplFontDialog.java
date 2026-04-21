package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplFontDialog extends ImplDialog {

    FontData _fontData();

    RGB _rgb();

    boolean _selected();

    int _fontID();

    int _fontSize();

    boolean _effectsVisible();
}
