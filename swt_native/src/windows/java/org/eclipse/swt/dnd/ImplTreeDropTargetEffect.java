package org.eclipse.swt.dnd;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplTreeDropTargetEffect extends ImplDropTargetEffect {

    long _dropIndex();

    long _scrollIndex();

    long _scrollBeginTime();

    long _expandIndex();

    long _expandBeginTime();

    TreeItem _insertItem();

    boolean _insertBefore();
}
