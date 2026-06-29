package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplTreeItem extends ImplItem {

    Tree _parent();

    String[] _strings();

    Image[] _images();

    Font _font();

    Font[] _cellFont();

    boolean _cached();

    int _background();

    int _foreground();

    int[] _cellBackground();

    int[] _cellForeground();
}
