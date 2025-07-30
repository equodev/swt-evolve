/**
 * ****************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
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
package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import java.util.WeakHashMap;

/**
 * Instances of this class represent the system task bar.
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 *
 * @see Display#getSystemTaskBar
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.6
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TaskBar extends Widget {

    TaskBar(Display display, int style) {
        this(new SWTTaskBar((SWTDisplay) display.delegate, style));
    }

    /**
     * Returns the item at the given, zero-relative index in the
     * receiver. Throws an exception if the index is out of range.
     *
     * @param index the index of the item to return
     * @return the item at the given index
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public TaskItem getItem(int index) {
        return TaskItem.getInstance(((ITaskBar) this.delegate).getItem(index));
    }

    /**
     * Returns the number of items contained in the receiver.
     *
     * @return the number of items
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getItemCount() {
        return ((ITaskBar) this.delegate).getItemCount();
    }

    /**
     * Returns the <code>TaskItem</code> for the given <code>Shell</code> or the <code>TaskItem</code>
     * for the application if the <code>Shell</code> parameter is <code>null</code>.
     * If the requested item is not supported by the platform it returns <code>null</code>.
     *
     * @param shell the shell for which the task item is requested, or null to request the application item
     * @return the task item for the given shell or the application
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public TaskItem getItem(Shell shell) {
        return TaskItem.getInstance(((ITaskBar) this.delegate).getItem((IShell) shell.delegate));
    }

    /**
     * Returns an array of <code>TaskItem</code>s which are the items
     * in the receiver.
     * <p>
     * Note: This is not the actual structure used by the receiver
     * to maintain its list of items, so modifying the array will
     * not affect the receiver.
     * </p>
     *
     * @return the items in the receiver
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public TaskItem[] getItems() {
        return TaskItem.ofArray(((ITaskBar) this.delegate).getItems(), TaskItem.class);
    }

    protected TaskBar(ITaskBar delegate) {
        super(delegate);
        this.delegate = delegate;
        INSTANCES.put(delegate, this);
    }

    public static TaskBar getInstance(ITaskBar delegate) {
        if (delegate == null) {
            return null;
        }
        TaskBar ref = (TaskBar) INSTANCES.get(delegate);
        if (ref == null) {
            ref = new TaskBar(delegate);
        }
        return ref;
    }
}
