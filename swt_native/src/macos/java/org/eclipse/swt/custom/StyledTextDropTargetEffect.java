/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * This adapter class provides a default drag under effect (eg. select and scroll)
 * when a drag occurs over a <code>StyledText</code>.
 *
 * <p>Classes that wish to provide their own drag under effect for a <code>StyledText</code>
 * can extend this class, override the <code>StyledTextDropTargetEffect.dragOver</code>
 * method and override any other applicable methods in <code>StyledTextDropTargetEffect</code> to
 * display their own drag under effect.</p>
 *
 * Subclasses that override any methods of this class should call the corresponding
 * <code>super</code> method to get the default drag under effect implementation.
 *
 * <p>The feedback value is either one of the FEEDBACK constants defined in
 * class <code>DND</code> which is applicable to instances of this class,
 * or it must be built by <em>bitwise OR</em>'ing together
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>DND</code> effect constants.
 * </p>
 * <dl>
 * <dt><b>Feedback:</b></dt>
 * <dd>FEEDBACK_SELECT, FEEDBACK_SCROLL</dd>
 * </dl>
 *
 * @see DropTargetAdapter
 * @see DropTargetEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.3
 */
public class StyledTextDropTargetEffect extends DropTargetEffect {

    /**
     * Creates a new <code>StyledTextDropTargetEffect</code> to handle the drag under effect on the specified
     * <code>StyledText</code>.
     *
     * @param styledText the <code>StyledText</code> over which the user positions the cursor to drop the data
     */
    public StyledTextDropTargetEffect(StyledText styledText) {
        this(new SwtStyledTextDropTargetEffect(styledText));
    }

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
    public void dragEnter(DropTargetEvent event) {
        getImpl().dragEnter(event);
    }

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
    public void dragLeave(DropTargetEvent event) {
        getImpl().dragLeave(event);
    }

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
    public void dragOver(DropTargetEvent event) {
        getImpl().dragOver(event);
    }

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
    public void dropAccept(DropTargetEvent event) {
        getImpl().dropAccept(event);
    }

    protected StyledTextDropTargetEffect(IStyledTextDropTargetEffect impl) {
        super(impl);
    }

    public static StyledTextDropTargetEffect createApi(IStyledTextDropTargetEffect impl) {
        return new StyledTextDropTargetEffect(impl);
    }

    public IStyledTextDropTargetEffect getImpl() {
        return (IStyledTextDropTargetEffect) super.getImpl();
    }
}
