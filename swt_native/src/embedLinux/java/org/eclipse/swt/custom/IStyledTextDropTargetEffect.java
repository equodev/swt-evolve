package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IStyledTextDropTargetEffect extends IDropTargetEffect {

    /**
     * This implementation of <code>dragEnter</code> provides a default drag under effect
     * for the feedback specified in <code>event.feedback</code>.
     *
     * For additional information see <code>DropTargetAdapter.dragEnter</code>.
     *
     * Subclasses that override this method should call <code>super.dragEnter(event)</code>
     * to get the default drag under effect implementation.
     *
     * @param event  the information associated with the drag start event
     *
     * @see DropTargetAdapter
     * @see DropTargetEvent
     */
    void dragEnter(DropTargetEvent event);

    /**
     * This implementation of <code>dragLeave</code> provides a default drag under effect
     * for the feedback specified in <code>event.feedback</code>.
     *
     * For additional information see <code>DropTargetAdapter.dragLeave</code>.
     *
     * Subclasses that override this method should call <code>super.dragLeave(event)</code>
     * to get the default drag under effect implementation.
     *
     * @param event  the information associated with the drag leave event
     *
     * @see DropTargetAdapter
     * @see DropTargetEvent
     */
    void dragLeave(DropTargetEvent event);

    /**
     * This implementation of <code>dragOver</code> provides a default drag under effect
     * for the feedback specified in <code>event.feedback</code>.
     *
     * For additional information see <code>DropTargetAdapter.dragOver</code>.
     *
     * Subclasses that override this method should call <code>super.dragOver(event)</code>
     * to get the default drag under effect implementation.
     *
     * @param event  the information associated with the drag over event
     *
     * @see DropTargetAdapter
     * @see DropTargetEvent
     * @see DND#FEEDBACK_SELECT
     * @see DND#FEEDBACK_SCROLL
     */
    void dragOver(DropTargetEvent event);

    /**
     * This implementation of <code>dropAccept</code> provides a default drag under effect
     * for the feedback specified in <code>event.feedback</code>.
     *
     * For additional information see <code>DropTargetAdapter.dropAccept</code>.
     *
     * Subclasses that override this method should call <code>super.dropAccept(event)</code>
     * to get the default drag under effect implementation.
     *
     * @param event  the information associated with the drop accept event
     *
     * @see DropTargetAdapter
     * @see DropTargetEvent
     */
    void dropAccept(DropTargetEvent event);

    StyledTextDropTargetEffect getApi();
}
