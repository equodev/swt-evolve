package org.eclipse.swt.dnd;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface ImplDropTarget extends ImplWidget {

    Control _control();

    Listener _controlListener();

    Transfer[] _transferAgents();

    DropTargetEffect _dropEffect();

    TransferData _selectedDataType();

    int _selectedOperation();

    int _keyOperation();

    long _dragOverStart();

    Runnable _dragOverHeartbeat();

    DNDEvent _dragOverEvent();

    int _drag_motion_handler();

    int _drag_leave_handler();

    int _drag_data_received_handler();

    int _drag_drop_handler();

    long _dropController();
}
