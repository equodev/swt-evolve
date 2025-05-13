package org.eclipse.swt.dnd;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public interface IDragSourceEffect extends IDragSourceAdapter {

    /**
     * Returns the Control which is registered for this DragSourceEffect.  This is the control that the
     * user clicks in to initiate dragging.
     *
     * @return the Control which is registered for this DragSourceEffect
     */
    IControl getControl();

    DragSourceEffect getApi();
}
