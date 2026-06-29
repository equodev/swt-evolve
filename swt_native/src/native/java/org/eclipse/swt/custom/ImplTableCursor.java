package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplTableCursor extends ImplCanvas {

    Table _table();

    TableItem _row();

    TableColumn _column();

    Listener _listener();

    Listener _tableListener();

    Listener _resizeListener();

    Listener _disposeItemListener();

    Listener _disposeColumnListener();

    Color _background();

    Color _foreground();
}
