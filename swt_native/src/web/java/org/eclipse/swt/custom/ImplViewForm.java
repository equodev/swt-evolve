package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplViewForm extends ImplComposite {

    Control _topLeft();

    Control _topCenter();

    Control _topRight();

    Control _content();

    boolean _separateTopCenter();

    boolean _showBorder();

    int _separator();

    int _borderTop();

    int _borderBottom();

    int _borderLeft();

    int _borderRight();

    int _highlight();

    Point _oldSize();

    Color _selectionBackground();

    Listener _listener();
}
