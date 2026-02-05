package org.eclipse.swt.dnd;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplTreeDropTargetEffect extends ImplDropTargetEffect {

    int _scrollIndex();

    long _scrollBeginTime();

    int _expandIndex();

    long _expandBeginTime();
}
