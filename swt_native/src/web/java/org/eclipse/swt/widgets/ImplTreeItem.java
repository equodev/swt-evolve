package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ImplTreeItem extends ImplItem {

    Tree _parent();

    TreeItem _parentItem();

    TreeItem[] _items();

    int _itemCount();

    String[] _strings();

    Image[] _images();

    boolean _checked();

    boolean _grayed();

    boolean _cached();

    boolean _expanded();

    Color _foreground();

    Color _background();

    Color[] _cellForeground();

    Color[] _cellBackground();

    Font _font();

    Font[] _cellFont();

    int _width();

    boolean isDrawing();

    void release(boolean destroy);
}
