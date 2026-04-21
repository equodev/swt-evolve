package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplToolItem extends ImplItem {

    int _width();

    ToolBar _parent();

    Image _hotImage();

    Image _disabledImage();

    Color _background();

    Color _foreground();

    String _toolTipText();

    Control _control();

    boolean _selection();

    boolean isDrawing();
}
