package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplTableItem extends ImplItem {

    Table _parent();

    String[] _strings();

    Image[] _images();

    Font _font();

    Font[] _cellFont();

    boolean _checked();

    boolean _grayed();

    boolean _cached();

    int _imageIndent();

    int _background();

    int _foreground();

    int[] _cellBackground();

    int[] _cellForeground();
}
