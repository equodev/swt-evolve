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
public class SwtTaskBar extends SwtWidget implements ITaskBar {

    int itemCount;

    TaskItem[] items = new TaskItem[4];

    SwtTaskBar(Display display, int style, TaskBar api) {
        super(api);
        if (display == null)
            display = SwtDisplay.getCurrent();
        if (display == null)
            display = SwtDisplay.getDefault();
        if (!((SwtDisplay) display.getImpl()).isValidThread()) {
            error(SWT.ERROR_THREAD_INVALID_ACCESS);
        }
        this.display = display;
        reskinWidget();
    }

    void createItem(TaskItem item, int index) {
        if (index == -1)
            index = itemCount;
        if (!(0 <= index && index <= itemCount))
            error(SWT.ERROR_INVALID_RANGE);
        if (itemCount == items.length) {
            TaskItem[] newItems = new TaskItem[items.length + 4];
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
        }
        System.arraycopy(items, index, items, index + 1, itemCount++ - index);
        items[index] = item;
    }

    void createItems() {
        getItem(null);
    }

    void destroyItem(TaskItem item) {
        int index = 0;
        while (index < itemCount) {
            if (items[index] == item)
                break;
            index++;
        }
        if (index == itemCount)
            return;
        System.arraycopy(items, index + 1, items, index, --itemCount - index);
        items[itemCount] = null;
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
        checkWidget();
        createItems();
        if (!(0 <= index && index < itemCount))
            error(SWT.ERROR_INVALID_RANGE);
        return items[index];
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
        checkWidget();
        createItems();
        return itemCount;
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
        checkWidget();
        for (int i = 0; i < itemCount; i++) {
            if (items[i] != null && ((SwtTaskItem) items[i].getImpl()).shell == shell) {
                return items[i];
            }
        }
        TaskItem item = null;
        if (shell == null) {
            item = new TaskItem(this.getApi(), SWT.NONE);
        } else {
            // on the Mac only the application item is supported
            //		TaskBarItem item = new TaskBarItem (this, SWT.NONE);
            //		item.setShell (shell);
        }
        return item;
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
        checkWidget();
        createItems();
        TaskItem[] result = new TaskItem[itemCount];
        System.arraycopy(items, 0, result, 0, result.length);
        return result;
    }

    @Override
    void releaseChildren(boolean destroy) {
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                TaskItem item = items[i];
                if (item != null && !item.isDisposed()) {
                    item.getImpl().release(false);
                }
            }
            items = null;
        }
        super.releaseChildren(destroy);
    }

    @Override
    void releaseParent() {
        super.releaseParent();
        if (((SwtDisplay) display.getImpl()).taskBar == this.getApi())
            ((SwtDisplay) display.getImpl()).taskBar = null;
    }

    @Override
    void reskinChildren(int flags) {
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                TaskItem item = items[i];
                if (item != null)
                    item.reskin(flags);
            }
        }
        super.reskinChildren(flags);
    }

    public TaskBar getApi() {
        if (api == null)
            api = TaskBar.createApi(this);
        return (TaskBar) api;
    }
}
