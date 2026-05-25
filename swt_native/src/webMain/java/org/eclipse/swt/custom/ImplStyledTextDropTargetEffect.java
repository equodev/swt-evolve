package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplStyledTextDropTargetEffect extends ImplDropTargetEffect {

    int _currentOffset();

    long _scrollBeginTime();

    int _scrollX();

    int _scrollY();

    Listener _paintListener();
}
