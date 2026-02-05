/**
 * ****************************************************************************
 *  Copyright (c) 2007, 2012 IBM Corporation and others.
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

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;
import dev.equo.swt.*;

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
public class DartTreeDropTargetEffect extends DartDropTargetEffect implements ITreeDropTargetEffect {

    // milli seconds
    static final int SCROLL_HYSTERESIS = 200;

    // milli seconds
    static final int EXPAND_HYSTERESIS = 1000;

    long dropIndex;

    long scrollIndex;

    long scrollBeginTime;

    long expandIndex;

    long expandBeginTime;

    TreeItem insertItem;

    boolean insertBefore;

    /**
     * Creates a new <code>TreeDropTargetEffect</code> to handle the drag under effect on the specified
     * <code>Tree</code>.
     *
     * @param tree the <code>Tree</code> over which the user positions the cursor to drop the data
     */
    public DartTreeDropTargetEffect(Tree tree, TreeDropTargetEffect api) {
        super(tree, api);
    }

    int checkEffect(int effect) {
        // Some effects are mutually exclusive.  Make sure that only one of the mutually exclusive effects has been specified.
        if ((effect & DND.FEEDBACK_SELECT) != 0)
            effect = effect & ~DND.FEEDBACK_INSERT_AFTER & ~DND.FEEDBACK_INSERT_BEFORE;
        if ((effect & DND.FEEDBACK_INSERT_BEFORE) != 0)
            effect = effect & ~DND.FEEDBACK_INSERT_AFTER;
        return effect;
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
    @Override
    public void dragEnter(DropTargetEvent event) {
        dropIndex = -1;
        insertItem = null;
        expandBeginTime = 0;
        expandIndex = -1;
        scrollBeginTime = 0;
        scrollIndex = -1;
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
    @Override
    public void dragLeave(DropTargetEvent event) {
        Tree tree = (Tree) control;
        if (dropIndex != -1) {
            dropIndex = -1;
        }
        if (insertItem != null) {
            tree.setInsertMark(null, false);
            insertItem = null;
        }
        expandBeginTime = 0;
        expandIndex = -1;
        scrollBeginTime = 0;
        scrollIndex = -1;
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
    @Override
    public void dragOver(DropTargetEvent event) {
        Tree tree = (Tree) getControl();
        int effect = checkEffect(event.feedback);
        if ((effect & DND.FEEDBACK_SCROLL) == 0) {
            scrollBeginTime = 0;
            scrollIndex = -1;
        } else {
            {
                scrollBeginTime = System.currentTimeMillis() + SCROLL_HYSTERESIS;
            }
        }
        if ((effect & DND.FEEDBACK_EXPAND) == 0) {
            expandBeginTime = 0;
            expandIndex = -1;
        } else {
            {
                expandBeginTime = System.currentTimeMillis() + EXPAND_HYSTERESIS;
            }
        }
        if ((effect & DND.FEEDBACK_INSERT_BEFORE) != 0 || (effect & DND.FEEDBACK_INSERT_AFTER) != 0) {
            boolean before = (effect & DND.FEEDBACK_INSERT_BEFORE) != 0;
            {
                if (insertItem != null) {
                    tree.setInsertMark(null, false);
                }
                insertItem = null;
            }
        } else {
            if (insertItem != null) {
                tree.setInsertMark(null, false);
            }
            insertItem = null;
        }
    }

    public long _dropIndex() {
        return dropIndex;
    }

    public long _scrollIndex() {
        return scrollIndex;
    }

    public long _scrollBeginTime() {
        return scrollBeginTime;
    }

    public long _expandIndex() {
        return expandIndex;
    }

    public long _expandBeginTime() {
        return expandBeginTime;
    }

    public TreeItem _insertItem() {
        return insertItem;
    }

    public boolean _insertBefore() {
        return insertBefore;
    }

    public TreeDropTargetEffect getApi() {
        if (api == null)
            api = TreeDropTargetEffect.createApi(this);
        return (TreeDropTargetEffect) api;
    }
}
