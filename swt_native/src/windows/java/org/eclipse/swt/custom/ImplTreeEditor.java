package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplTreeEditor extends ImplControlEditor {

    Tree _tree();

    TreeItem _item();

    int _column();

    ControlListener _columnListener();

    TreeListener _treeListener();

    Runnable _timer();
}
