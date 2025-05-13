package org.eclipse.swt.dnd;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IDropTargetEffect extends IDropTargetAdapter {

    /**
     * Returns the Control which is registered for this DropTargetEffect.  This is the control over which the
     * user positions the cursor to drop the data.
     *
     * @return the Control which is registered for this DropTargetEffect
     */
    IControl getControl();

    /**
     * Returns the item at the given x-y coordinate in the receiver
     * or null if no such item exists. The x-y coordinate is in the
     * display relative coordinates.
     *
     * @param x the x coordinate used to locate the item
     * @param y the y coordinate used to locate the item
     * @return the item at the given x-y coordinate, or null if the coordinate is not in a selectable item
     */
    IWidget getItem(int x, int y);

    DropTargetEffect getApi();
}
