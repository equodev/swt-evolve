package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public interface ImplMenu extends ImplWidget {

    int _x();

    int _y();

    boolean _hasLocation();

    MenuItem _cascade();

    MenuItem _selectedItem();

    Decorations _parent();

    int _poppedUpCount();

    long _menuHandle();

    long _modelHandle();

    long _actionGroup();

    long _shortcutController();

    void _setVisible(boolean visible);
}
