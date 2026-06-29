package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplTreeCursor extends ImplCanvas {

    Tree _tree();

    TreeItem _row();

    TreeColumn _column();

    Listener _listener();

    Listener _treeListener();

    Listener _resizeListener();

    Listener _disposeItemListener();

    Listener _disposeColumnListener();

    Color _background();

    Color _foreground();
}
