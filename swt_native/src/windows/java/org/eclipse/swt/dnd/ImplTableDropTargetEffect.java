package org.eclipse.swt.dnd;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplTableDropTargetEffect extends ImplDropTargetEffect {

    int _scrollIndex();

    long _scrollBeginTime();

    TableItem _dropHighlight();

    int _iItemInsert();
}
