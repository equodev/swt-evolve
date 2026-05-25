package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplTableItem extends ImplItem {

    Table _parent();

    String[] _strings();

    Image[] _images();

    boolean _checked();

    boolean _grayed();

    boolean _cached();

    Color _foreground();

    Color _background();

    Color[] _cellForeground();

    Color[] _cellBackground();

    Font _font();

    Font[] _cellFont();

    int _width();

    boolean isDrawing();
}
