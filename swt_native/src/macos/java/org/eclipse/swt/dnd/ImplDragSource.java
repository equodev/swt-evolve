package org.eclipse.swt.dnd;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplDragSource extends ImplWidget {

    String[] _paths();

    boolean[] _exist();

    Control _control();

    Listener _controlListener();

    Transfer[] _transferAgents();

    DragSourceEffect _dragEffect();

    Image _dragImageFromListener();
}
