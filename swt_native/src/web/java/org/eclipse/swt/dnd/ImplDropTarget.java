package org.eclipse.swt.dnd;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplDropTarget extends ImplWidget {

    Control _control();

    Listener _controlListener();

    Transfer[] _transferAgents();

    DropTargetEffect _dropEffect();

    int _feedback();

    boolean _labelDragHandlersAdded();

    TransferData _selectedDataType();

    int _selectedOperation();

    int _keyOperation();
}
