/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2020 IBM Corporation and others.
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
 *      Christoph Läubrich - Bug 513185
 * *****************************************************************************
 */
package org.eclipse.swt.layout;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * <code>FillLayout</code> is the simplest layout class. It lays out
 * controls in a single row or column, forcing them to be the same size.
 * <p>
 * Initially, the controls will all be as tall as the tallest control,
 * and as wide as the widest. <code>FillLayout</code> does not wrap,
 * but you can specify margins and spacing. You might use it to
 * lay out buttons in a task bar or tool bar, or to stack checkboxes
 * in a <code>Group</code>. <code>FillLayout</code> can also be used
 * when a <code>Composite</code> only has one child. For example,
 * if a <code>Shell</code> has a single <code>Group</code> child,
 * <code>FillLayout</code> will cause the <code>Group</code> to
 * completely fill the <code>Shell</code> (if margins are 0).
 * </p>
 * <p>
 * Example code: first a <code>FillLayout</code> is created and
 * its type field is set, and then the layout is set into the
 * <code>Composite</code>. Note that in a <code>FillLayout</code>,
 * children are always the same size, and they fill all available space.
 * </p>
 * <pre>
 * 		FillLayout fillLayout = new FillLayout();
 * 		fillLayout.type = SWT.VERTICAL;
 * 		shell.setLayout(fillLayout);
 * </pre>
 *
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: LayoutExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class FillLayout extends Layout {

    /**
     * type specifies how controls will be positioned
     * within the layout.
     *
     * The default value is HORIZONTAL.
     *
     * Possible values are: <ul>
     *    <li>HORIZONTAL: Position the controls horizontally from left to right</li>
     *    <li>VERTICAL: Position the controls vertically from top to bottom</li>
     * </ul>
     */
    public int type = SWT.HORIZONTAL;

    /**
     * marginWidth specifies the number of points of horizontal margin
     * that will be placed along the left and right edges of the layout.
     *
     * The default value is 0.
     *
     * @since 3.0
     */
    public int marginWidth = 0;

    /**
     * marginHeight specifies the number of points of vertical margin
     * that will be placed along the top and bottom edges of the layout.
     *
     * The default value is 0.
     *
     * @since 3.0
     */
    public int marginHeight = 0;

    /**
     * spacing specifies the number of points between the edge of one cell
     * and the edge of its neighbouring cell.
     *
     * The default value is 0.
     *
     * @since 3.0
     */
    public int spacing = 0;

    /**
     * Constructs a new instance of this class.
     */
    public FillLayout() {
    }

    /**
     * Constructs a new instance of this class given the type.
     *
     * @param type the type of fill layout
     *
     * @since 2.0
     */
    public FillLayout(int type) {
        this.type = type;
    }

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        Control[] children = composite.getChildren();
        int count = children.length;
        int maxWidth = 0, maxHeight = 0;
        for (int i = 0; i < count; i++) {
            Control child = children[i];
            int w = wHint, h = hHint;
            if (count > 0) {
                if (type == SWT.HORIZONTAL && wHint != SWT.DEFAULT) {
                    w = Math.max(0, (wHint - (count - 1) * spacing) / count);
                }
                if (type == SWT.VERTICAL && hHint != SWT.DEFAULT) {
                    h = Math.max(0, (hHint - (count - 1) * spacing) / count);
                }
            }
            Point size = computeChildSize(child, w, h, flushCache);
            maxWidth = Math.max(maxWidth, size.x);
            maxHeight = Math.max(maxHeight, size.y);
        }
        int width = 0, height = 0;
        if (type == SWT.HORIZONTAL) {
            width = count * maxWidth;
            if (count != 0)
                width += (count - 1) * spacing;
            height = maxHeight;
        } else {
            width = maxWidth;
            height = count * maxHeight;
            if (count != 0)
                height += (count - 1) * spacing;
        }
        width += marginWidth * 2;
        height += marginHeight * 2;
        if (wHint != SWT.DEFAULT)
            width = wHint;
        if (hHint != SWT.DEFAULT)
            height = hHint;
        return new Point(width, height);
    }

    Point computeChildSize(Control control, int wHint, int hHint, boolean flushCache) {
        Object data = control.getLayoutData();
        FillData fillData;
        if (data instanceof FillData) {
            fillData = (FillData) data;
        } else {
            fillData = new FillData();
            if (data == null) {
                control.setLayoutData(fillData);
            }
        }
        Point size = null;
        if (wHint == SWT.DEFAULT && hHint == SWT.DEFAULT) {
            size = fillData.computeSize(control, wHint, hHint, flushCache);
        } else {
            // TEMPORARY CODE
            int trimX, trimY;
            if (control instanceof IScrollable) {
                Rectangle rect = ((IScrollable) control).computeTrim(0, 0, 0, 0);
                trimX = rect.width;
                trimY = rect.height;
            } else {
                trimX = trimY = control.getBorderWidth() * 2;
            }
            int w = wHint == SWT.DEFAULT ? wHint : Math.max(0, wHint - trimX);
            int h = hHint == SWT.DEFAULT ? hHint : Math.max(0, hHint - trimY);
            size = fillData.computeSize(control, w, h, flushCache);
        }
        return size;
    }

    @Override
    protected boolean flushCache(Control control) {
        Object data = control.getLayoutData();
        if (data instanceof FillData) {
            ((FillData) data).flushCache();
            return true;
        }
        return false;
    }

    String getName() {
        String string = getClass().getName();
        int index = string.lastIndexOf('.');
        if (index == -1)
            return string;
        return string.substring(index + 1, string.length());
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
        Rectangle rect = composite.getClientArea();
        Control[] children = composite.getChildren();
        int count = children.length;
        if (count == 0)
            return;
        int width = rect.width - marginWidth * 2;
        int height = rect.height - marginHeight * 2;
        if (type == SWT.HORIZONTAL) {
            width -= (count - 1) * spacing;
            int x = rect.x + marginWidth, extra = width % count;
            int y = rect.y + marginHeight, cellWidth = width / count;
            for (int i = 0; i < count; i++) {
                Control child = children[i];
                int childWidth = cellWidth;
                if (i == 0) {
                    childWidth += extra / 2;
                } else {
                    if (i == count - 1)
                        childWidth += (extra + 1) / 2;
                }
                child.setBounds(x, y, childWidth, height);
                x += childWidth + spacing;
            }
        } else {
            height -= (count - 1) * spacing;
            int x = rect.x + marginWidth, cellHeight = height / count;
            int y = rect.y + marginHeight, extra = height % count;
            for (int i = 0; i < count; i++) {
                Control child = children[i];
                int childHeight = cellHeight;
                if (i == 0) {
                    childHeight += extra / 2;
                } else {
                    if (i == count - 1)
                        childHeight += (extra + 1) / 2;
                }
                child.setBounds(x, y, width, childHeight);
                y += childHeight + spacing;
            }
        }
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the layout
     */
    @Override
    public String toString() {
        String string = getName() + " {";
        string += "type=" + ((type == SWT.VERTICAL) ? "SWT.VERTICAL" : "SWT.HORIZONTAL") + " ";
        if (marginWidth != 0)
            string += "marginWidth=" + marginWidth + " ";
        if (marginHeight != 0)
            string += "marginHeight=" + marginHeight + " ";
        if (spacing != 0)
            string += "spacing=" + spacing + " ";
        string = string.trim();
        string += "}";
        return string;
    }
}
