package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplTableEditor extends ImplControlEditor {

    Table _table();

    TableItem _item();

    int _column();

    ControlListener _columnListener();

    Runnable _timer();
}
