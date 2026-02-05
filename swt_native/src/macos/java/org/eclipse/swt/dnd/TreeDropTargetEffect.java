/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.swt.dnd;

import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.Config;

/**
 * This class provides a default drag under effect (eg. select, insert, scroll and expand)
 * when a drag occurs over a <code>Tree</code>.
 *
 * <p>Classes that wish to provide their own drag under effect for a <code>Tree</code>
 * can extend the <code>TreeDropTargetEffect</code> class and override any applicable methods
 * in <code>TreeDropTargetEffect</code> to display their own drag under effect.</p>
 *
 * Subclasses that override any methods of this class must call the corresponding
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
 * <dd>FEEDBACK_SELECT, FEEDBACK_INSERT_BEFORE, FEEDBACK_INSERT_AFTER, FEEDBACK_EXPAND, FEEDBACK_SCROLL</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles FEEDBACK_SELECT, FEEDBACK_INSERT_BEFORE or
 * FEEDBACK_INSERT_AFTER may be specified.
 * </p>
 *
 * @see DropTargetAdapter
 * @see DropTargetEvent
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.3
 */
public class TreeDropTargetEffect extends DropTargetEffect {

    /**
     * Creates a new <code>TreeDropTargetEffect</code> to handle the drag under effect on the specified
     * <code>Tree</code>.
     *
     * @param tree the <code>Tree</code> over which the user positions the cursor to drop the data
     */
    public TreeDropTargetEffect(Tree tree) {
        this((ITreeDropTargetEffect) null);
        setImpl(Config.isEquo(TreeDropTargetEffect.class) ? new DartTreeDropTargetEffect(tree, this) : new SwtTreeDropTargetEffect(tree, this));
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
     * @param event  the information associated with the drag enter event
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
     * @see DND#FEEDBACK_INSERT_BEFORE
     * @see DND#FEEDBACK_INSERT_AFTER
     * @see DND#FEEDBACK_SCROLL
     */
    public void dragOver(DropTargetEvent event) {
        getImpl().dragOver(event);
    }

    protected TreeDropTargetEffect(ITreeDropTargetEffect impl) {
        super(impl);
    }

    static TreeDropTargetEffect createApi(ITreeDropTargetEffect impl) {
        return new TreeDropTargetEffect(impl);
    }

    public ITreeDropTargetEffect getImpl() {
        return (ITreeDropTargetEffect) super.getImpl();
    }
}
