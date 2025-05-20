/**
 * ****************************************************************************
 *  Copyright (c) 2011, 2016 IBM Corporation and others.
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
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * A TreeCursor provides a way for the user to navigate around a Tree with columns using the
 * keyboard. It also provides a mechanism for selecting an individual cell in a tree.
 * <p>
 * For a detailed example of using a TreeCursor to navigate to a cell and then edit it see
 * https://github.com/eclipse-platform/eclipse.platform.swt/tree/master/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet360.java .
 *
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection</dd>
 * </dl>
 *
 * @since 3.8
 */
public class TreeCursor extends Canvas {

    /**
     * Constructs a new instance of this class given its parent tree and a style value describing
     * its behavior and appearance.
     * <p>
     * The style value is either one of the style constants defined in class <code>SWT</code> which
     * is applicable to instances of this class, or must be built by <em>bitwise OR</em>'ing
     * together (that is, using the <code>int</code> "|" operator) two or more of those
     * <code>SWT</code> style constants. The class description lists the style constants that are
     * applicable to the class. Style bits are also inherited from superclasses.
     * </p>
     *
     * @param parent a Tree control which will be the parent of the new instance (cannot be null)
     * @param style the style of control to construct
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
     *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
     * </ul>
     *
     * @see SWT#BORDER
     * @see Widget#checkSubclass()
     * @see Widget#getStyle()
     */
    public TreeCursor(Tree parent, int style) {
        this((ITreeCursor) null);
        setImpl(new SwtTreeCursor(parent, style));
    }

    /**
     * Adds the listener to the collection of listeners who will be notified when the receiver's
     * selection changes, by sending it one of the messages defined in the
     * <code>SelectionListener</code> interface.
     * <p>
     * When <code>widgetSelected</code> is called, the item field of the event object is valid. If
     * the receiver has <code>SWT.CHECK</code> style set and the check selection changes, the event
     * object detail field contains the value <code>SWT.CHECK</code>.
     * <code>widgetDefaultSelected</code> is typically called when an item is double-clicked.
     * </p>
     *
     * @param listener the listener which should be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SelectionListener
     * @see SelectionEvent
     * @see #removeSelectionListener(SelectionListener)
     */
    public void addSelectionListener(SelectionListener listener) {
        getImpl().addSelectionListener(listener);
    }

    /**
     * Returns the background color that the receiver will use to draw.
     *
     * @return the receiver's background color
     */
    public Color getBackground() {
        return getImpl().getBackground();
    }

    /**
     * Returns the index of the column over which the TreeCursor is positioned.
     *
     * @return the column index for the current position
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public int getColumn() {
        return getImpl().getColumn();
    }

    /**
     * Returns the foreground color that the receiver will use to draw.
     *
     * @return the receiver's foreground color
     */
    public Color getForeground() {
        return getImpl().getForeground();
    }

    /**
     * Returns the row over which the TreeCursor is positioned.
     *
     * @return the item for the current position, or <code>null</code> if none
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public TreeItem getRow() {
        return getImpl().getRow();
    }

    /**
     * Removes the listener from the collection of listeners who will be notified when the
     * receiver's selection changes.
     *
     * @param listener the listener which should no longer be notified
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     *
     * @see SelectionListener
     * @see #addSelectionListener(SelectionListener)
     */
    public void removeSelectionListener(SelectionListener listener) {
        getImpl().removeSelectionListener(listener);
    }

    /**
     * Sets the receiver's background color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * For example, on Windows the background of a Button cannot be changed.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setBackground(Color color) {
        getImpl().setBackground(color);
    }

    /**
     * Sets the receiver's foreground color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * <p>
     * Note: This operation is a hint and may be overridden by the platform.
     * </p>
     * @param color the new color (or null)
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
     * </ul>
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setForeground(Color color) {
        getImpl().setForeground(color);
    }

    /**
     * Positions the TreeCursor over the root-level cell at the given row and column in the parent tree.
     *
     * @param row the index of the root-level row for the cell to select
     * @param column the index of column for the cell to select
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(int row, int column) {
        getImpl().setSelection(row, column);
    }

    /**
     * Positions the TreeCursor over the cell at the given row and column in the parent tree.
     *
     * @param row the TreeItem of the row for the cell to select
     * @param column the index of column for the cell to select
     *
     * @exception SWTException <ul>
     *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
     *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
     * </ul>
     */
    public void setSelection(TreeItem row, int column) {
        getImpl().setSelection(row, column);
    }

    public void setVisible(boolean visible) {
        getImpl().setVisible(visible);
    }

    protected TreeCursor(ITreeCursor impl) {
        super(impl);
    }

    static TreeCursor createApi(ITreeCursor impl) {
        if (dev.equo.swt.Creation.creating.peek() instanceof TreeCursor inst) {
            inst.impl = impl;
            return inst;
        } else
            return new TreeCursor(impl);
    }

    public ITreeCursor getImpl() {
        return (ITreeCursor) super.getImpl();
    }
}
