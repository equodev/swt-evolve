package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplColorDialog extends ImplDialog {

    RGB _rgb();

    RGB[] _rgbs();

    boolean _selected();
}
