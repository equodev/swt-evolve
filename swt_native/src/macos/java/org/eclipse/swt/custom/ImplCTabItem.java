package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplCTabItem extends ImplItem {

    CTabFolder _parent();

    int _x();

    int _y();

    int _width();

    int _height();

    Control _control();

    String _toolTipText();

    String _shortenedText();

    int _shortenedTextWidth();

    Font _font();

    Color _selectionForeground();

    Image _disabledImage();

    Rectangle _closeRect();

    int _closeImageState();

    boolean _showClose();

    boolean _showing();
}
