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

    String _toolTipText();

    Control _control();

    boolean _selection();

    boolean isDrawing();
}
