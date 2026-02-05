package org.eclipse.swt.dnd;

import java.lang.reflect.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplDragSource extends ImplWidget {

    Control _control();

    Listener _controlListener();

    Transfer[] _transferAgents();

    DragSourceEffect _dragEffect();

    long _targetList();

    boolean _moveData();
}
