/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.swt.layout;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * Instances of this class determine the size and position of the
 * children of a <code>Composite</code> by placing them either in
 * horizontal rows or vertical columns within the parent <code>Composite</code>.
 * <p>
 * <code>RowLayout</code> aligns all controls in one row if the
 * <code>type</code> is set to horizontal, and one column if it is
 * set to vertical. It has the ability to wrap, and provides configurable
 * margins and spacing. <code>RowLayout</code> has a number of configuration
 * fields. In addition, the height and width of each control in a
 * <code>RowLayout</code> can be specified by setting a <code>RowData</code>
 * object into the control using <code>setLayoutData ()</code>.
 * </p>
 * <p>
 * The following example code creates a <code>RowLayout</code>, sets all
 * of its fields to non-default values, and then sets it into a
 * <code>Shell</code>.</p>
 * <pre>
 * 		RowLayout rowLayout = new RowLayout();
 * 		rowLayout.wrap = false;
 * 		rowLayout.pack = false;
 * 		rowLayout.justify = true;
 * 		rowLayout.type = SWT.VERTICAL;
 * 		rowLayout.marginLeft = 5;
 * 		rowLayout.marginTop = 5;
 * 		rowLayout.marginRight = 5;
 * 		rowLayout.marginBottom = 5;
 * 		rowLayout.spacing = 0;
 * 		shell.setLayout(rowLayout);
 * </pre>
 * If you are using the default field values, you only need one line of code:
 * <pre>
 * 		shell.setLayout(new RowLayout());
 * </pre>
 *
 * @see RowData
 * @see <a href="http://www.eclipse.org/swt/snippets/#rowlayout">RowLayout snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: LayoutExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class SwtRowLayout extends SwtLayout implements IRowLayout {

    /**
     * Constructs a new instance of this class with type HORIZONTAL.
     */
    public SwtRowLayout(RowLayout api) {
        super(api);
    }

    /**
     * Constructs a new instance of this class given the type.
     *
     * @param type the type of row layout
     *
     * @since 2.0
     */
    public SwtRowLayout(int type, RowLayout api) {
        super(api);
        this.getApi().type = type;
    }

    @Override
    public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        Point extent;
        if (getApi().type == SWT.HORIZONTAL) {
            extent = layoutHorizontal(composite, false, (wHint != SWT.DEFAULT) && getApi().wrap, wHint, flushCache);
        } else {
            extent = layoutVertical(composite, false, (hHint != SWT.DEFAULT) && getApi().wrap, hHint, flushCache);
        }
        if (wHint != SWT.DEFAULT)
            extent.x = wHint;
        if (hHint != SWT.DEFAULT)
            extent.y = hHint;
        return extent;
    }

    Point computeSize(Control control, boolean flushCache) {
        int wHint = SWT.DEFAULT, hHint = SWT.DEFAULT;
        RowData data = (RowData) control.getLayoutData();
        if (data != null) {
            wHint = data.width;
            hHint = data.height;
        }
        return control.computeSize(wHint, hHint, flushCache);
    }

    @Override
    public boolean flushCache(Control control) {
        return true;
    }

    String getName() {
        String string = getClass().getName();
        int index = string.lastIndexOf('.');
        if (index == -1)
            return string;
        return string.substring(index + 1, string.length());
    }

    @Override
    public void layout(Composite composite, boolean flushCache) {
        Rectangle clientArea = composite.getClientArea();
        if (getApi().type == SWT.HORIZONTAL) {
            layoutHorizontal(composite, true, getApi().wrap, clientArea.width, flushCache);
        } else {
            layoutVertical(composite, true, getApi().wrap, clientArea.height, flushCache);
        }
    }

    Point layoutHorizontal(Composite composite, boolean move, boolean wrap, int width, boolean flushCache) {
        Control[] children = composite.getChildren();
        int count = 0;
        for (int i = 0; i < children.length; i++) {
            Control control = children[i];
            RowData data = (RowData) control.getLayoutData();
            if (data == null || !data.exclude) {
                children[count++] = children[i];
            }
        }
        if (count == 0) {
            return new Point(getApi().marginLeft + getApi().marginWidth * 2 + getApi().marginRight, getApi().marginTop + getApi().marginHeight * 2 + getApi().marginBottom);
        }
        int childWidth = 0, childHeight = 0, maxHeight = 0;
        if (!getApi().pack) {
            for (int i = 0; i < count; i++) {
                Control child = children[i];
                Point size = computeSize(child, flushCache);
                if (width > SWT.DEFAULT && width < size.x && wrap) {
                    size = child.computeSize(width, child.getLayoutData() == null ? SWT.DEFAULT : ((RowData) child.getLayoutData()).height, flushCache);
                }
                childWidth = Math.max(childWidth, size.x);
                childHeight = Math.max(childHeight, size.y);
            }
            maxHeight = childHeight;
        }
        int clientX = 0, clientY = 0;
        if (move) {
            Rectangle rect = composite.getClientArea();
            clientX = rect.x;
            clientY = rect.y;
        }
        int[] wraps = null;
        boolean wrapped = false;
        Rectangle[] bounds = null;
        if (move && (getApi().justify || getApi().fill || getApi().center)) {
            bounds = new Rectangle[count];
            wraps = new int[count];
        }
        int maxX = 0, x = getApi().marginLeft + getApi().marginWidth, y = getApi().marginTop + getApi().marginHeight;
        for (int i = 0; i < count; i++) {
            Control child = children[i];
            if (getApi().pack) {
                Point size = computeSize(child, flushCache);
                if (width > SWT.DEFAULT && width < size.x && wrap) {
                    size = child.computeSize(width, child.getLayoutData() == null ? SWT.DEFAULT : ((RowData) child.getLayoutData()).height, flushCache);
                }
                childWidth = size.x;
                childHeight = size.y;
            }
            if (wrap && (i != 0) && (x + childWidth > width)) {
                wrapped = true;
                if (move && (getApi().justify || getApi().fill || getApi().center))
                    wraps[i - 1] = maxHeight;
                x = getApi().marginLeft + getApi().marginWidth;
                y += getApi().spacing + maxHeight;
                if (getApi().pack)
                    maxHeight = 0;
            }
            if (getApi().pack || getApi().fill || getApi().center) {
                maxHeight = Math.max(maxHeight, childHeight);
            }
            if (move) {
                int childX = x + clientX, childY = y + clientY;
                if (getApi().justify || getApi().fill || getApi().center) {
                    bounds[i] = new Rectangle(childX, childY, childWidth, childHeight);
                } else {
                    child.setBounds(childX, childY, childWidth, childHeight);
                }
            }
            x += getApi().spacing + childWidth;
            maxX = Math.max(maxX, x);
        }
        maxX = Math.max(clientX + getApi().marginLeft + getApi().marginWidth, maxX - getApi().spacing);
        if (!wrapped)
            maxX += getApi().marginRight + getApi().marginWidth;
        if (move && (getApi().justify || getApi().fill || getApi().center)) {
            int space = 0, margin = 0;
            if (!wrapped) {
                space = Math.max(0, (width - maxX) / (count + 1));
                margin = Math.max(0, ((width - maxX) % (count + 1)) / 2);
            } else {
                if (getApi().fill || getApi().justify || getApi().center) {
                    int last = 0;
                    if (count > 0)
                        wraps[count - 1] = maxHeight;
                    for (int i = 0; i < count; i++) {
                        if (wraps[i] != 0) {
                            int wrapCount = i - last + 1;
                            if (getApi().justify) {
                                int wrapX = 0;
                                for (int j = last; j <= i; j++) {
                                    wrapX += bounds[j].width + getApi().spacing;
                                }
                                space = Math.max(0, (width - wrapX) / (wrapCount + 1));
                                margin = Math.max(0, ((width - wrapX) % (wrapCount + 1)) / 2);
                            }
                            for (int j = last; j <= i; j++) {
                                if (getApi().justify)
                                    bounds[j].x += (space * (j - last + 1)) + margin;
                                if (getApi().fill) {
                                    bounds[j].height = wraps[i];
                                } else {
                                    if (getApi().center) {
                                        bounds[j].y += Math.max(0, (wraps[i] - bounds[j].height) / 2);
                                    }
                                }
                            }
                            last = i + 1;
                        }
                    }
                }
            }
            for (int i = 0; i < count; i++) {
                if (!wrapped) {
                    if (getApi().justify)
                        bounds[i].x += (space * (i + 1)) + margin;
                    if (getApi().fill) {
                        bounds[i].height = maxHeight;
                    } else {
                        if (getApi().center) {
                            bounds[i].y += Math.max(0, (maxHeight - bounds[i].height) / 2);
                        }
                    }
                }
                children[i].setBounds(bounds[i]);
            }
        }
        return new Point(maxX, y + maxHeight + getApi().marginBottom + getApi().marginHeight);
    }

    Point layoutVertical(Composite composite, boolean move, boolean wrap, int height, boolean flushCache) {
        Control[] children = composite.getChildren();
        int count = 0;
        for (int i = 0; i < children.length; i++) {
            Control control = children[i];
            RowData data = (RowData) control.getLayoutData();
            if (data == null || !data.exclude) {
                children[count++] = children[i];
            }
        }
        if (count == 0) {
            return new Point(getApi().marginLeft + getApi().marginWidth * 2 + getApi().marginRight, getApi().marginTop + getApi().marginHeight * 2 + getApi().marginBottom);
        }
        int childWidth = 0, childHeight = 0, maxWidth = 0;
        if (!getApi().pack) {
            for (int i = 0; i < count; i++) {
                Control child = children[i];
                Point size = computeSize(child, flushCache);
                if (height > SWT.DEFAULT && height < size.y && wrap)
                    size = child.computeSize(child.getLayoutData() == null ? SWT.DEFAULT : ((RowData) child.getLayoutData()).width, height, flushCache);
                childWidth = Math.max(childWidth, size.x);
                childHeight = Math.max(childHeight, size.y);
            }
            maxWidth = childWidth;
        }
        int clientX = 0, clientY = 0;
        if (move) {
            Rectangle rect = composite.getClientArea();
            clientX = rect.x;
            clientY = rect.y;
        }
        int[] wraps = null;
        boolean wrapped = false;
        Rectangle[] bounds = null;
        if (move && (getApi().justify || getApi().fill || getApi().center)) {
            bounds = new Rectangle[count];
            wraps = new int[count];
        }
        int maxY = 0, x = getApi().marginLeft + getApi().marginWidth, y = getApi().marginTop + getApi().marginHeight;
        for (int i = 0; i < count; i++) {
            Control child = children[i];
            if (getApi().pack) {
                Point size = computeSize(child, flushCache);
                if (height > SWT.DEFAULT && height < size.y && wrap)
                    size = child.computeSize(child.getLayoutData() == null ? SWT.DEFAULT : ((RowData) child.getLayoutData()).width, height, flushCache);
                childWidth = size.x;
                childHeight = size.y;
            }
            if (wrap && (i != 0) && (y + childHeight > height)) {
                wrapped = true;
                if (move && (getApi().justify || getApi().fill || getApi().center))
                    wraps[i - 1] = maxWidth;
                x += getApi().spacing + maxWidth;
                y = getApi().marginTop + getApi().marginHeight;
                if (getApi().pack)
                    maxWidth = 0;
            }
            if (getApi().pack || getApi().fill || getApi().center) {
                maxWidth = Math.max(maxWidth, childWidth);
            }
            if (move) {
                int childX = x + clientX, childY = y + clientY;
                if (getApi().justify || getApi().fill || getApi().center) {
                    bounds[i] = new Rectangle(childX, childY, childWidth, childHeight);
                } else {
                    child.setBounds(childX, childY, childWidth, childHeight);
                }
            }
            y += getApi().spacing + childHeight;
            maxY = Math.max(maxY, y);
        }
        maxY = Math.max(clientY + getApi().marginTop + getApi().marginHeight, maxY - getApi().spacing);
        if (!wrapped)
            maxY += getApi().marginBottom + getApi().marginHeight;
        if (move && (getApi().justify || getApi().fill || getApi().center)) {
            int space = 0, margin = 0;
            if (!wrapped) {
                space = Math.max(0, (height - maxY) / (count + 1));
                margin = Math.max(0, ((height - maxY) % (count + 1)) / 2);
            } else {
                if (getApi().fill || getApi().justify || getApi().center) {
                    int last = 0;
                    if (count > 0)
                        wraps[count - 1] = maxWidth;
                    for (int i = 0; i < count; i++) {
                        if (wraps[i] != 0) {
                            int wrapCount = i - last + 1;
                            if (getApi().justify) {
                                int wrapY = 0;
                                for (int j = last; j <= i; j++) {
                                    wrapY += bounds[j].height + getApi().spacing;
                                }
                                space = Math.max(0, (height - wrapY) / (wrapCount + 1));
                                margin = Math.max(0, ((height - wrapY) % (wrapCount + 1)) / 2);
                            }
                            for (int j = last; j <= i; j++) {
                                if (getApi().justify)
                                    bounds[j].y += (space * (j - last + 1)) + margin;
                                if (getApi().fill) {
                                    bounds[j].width = wraps[i];
                                } else {
                                    if (getApi().center) {
                                        bounds[j].x += Math.max(0, (wraps[i] - bounds[j].width) / 2);
                                    }
                                }
                            }
                            last = i + 1;
                        }
                    }
                }
            }
            for (int i = 0; i < count; i++) {
                if (!wrapped) {
                    if (getApi().justify)
                        bounds[i].y += (space * (i + 1)) + margin;
                    if (getApi().fill) {
                        bounds[i].width = maxWidth;
                    } else {
                        if (getApi().center) {
                            bounds[i].x += Math.max(0, (maxWidth - bounds[i].width) / 2);
                        }
                    }
                }
                children[i].setBounds(bounds[i]);
            }
        }
        return new Point(x + maxWidth + getApi().marginRight + getApi().marginWidth, maxY);
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
        string += "type=" + ((getApi().type != SWT.HORIZONTAL) ? "SWT.VERTICAL" : "SWT.HORIZONTAL") + " ";
        if (getApi().marginWidth != 0)
            string += "marginWidth=" + getApi().marginWidth + " ";
        if (getApi().marginHeight != 0)
            string += "marginHeight=" + getApi().marginHeight + " ";
        if (getApi().marginLeft != 0)
            string += "marginLeft=" + getApi().marginLeft + " ";
        if (getApi().marginTop != 0)
            string += "marginTop=" + getApi().marginTop + " ";
        if (getApi().marginRight != 0)
            string += "marginRight=" + getApi().marginRight + " ";
        if (getApi().marginBottom != 0)
            string += "marginBottom=" + getApi().marginBottom + " ";
        if (getApi().spacing != 0)
            string += "spacing=" + getApi().spacing + " ";
        string += "wrap=" + getApi().wrap + " ";
        string += "pack=" + getApi().pack + " ";
        string += "fill=" + getApi().fill + " ";
        string += "justify=" + getApi().justify + " ";
        string = string.trim();
        string += "}";
        return string;
    }

    public RowLayout getApi() {
        if (api == null)
            api = RowLayout.createApi(this);
        return (RowLayout) api;
    }
}
