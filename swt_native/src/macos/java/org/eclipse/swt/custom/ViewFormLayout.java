/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
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
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * This class provides the layout for ViewForm
 *
 * @see ViewForm
 */
class ViewFormLayout extends SwtLayout {

    @Override
    public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        ViewForm form = (ViewForm) composite;
        Control left = ((SwtViewForm) form.getImpl()).topLeft;
        Control center = ((SwtViewForm) form.getImpl()).topCenter;
        Control right = ((SwtViewForm) form.getImpl()).topRight;
        Control content = ((SwtViewForm) form.getImpl()).content;
        Point leftSize = new Point(0, 0);
        if (left != null) {
            leftSize = computeChildSize(left, SWT.DEFAULT, SWT.DEFAULT, flushCache);
        }
        Point centerSize = new Point(0, 0);
        if (center != null) {
            centerSize = computeChildSize(center, SWT.DEFAULT, SWT.DEFAULT, flushCache);
        }
        Point rightSize = new Point(0, 0);
        if (right != null) {
            rightSize = computeChildSize(right, SWT.DEFAULT, SWT.DEFAULT, flushCache);
        }
        Point size = new Point(0, 0);
        // calculate width of title bar
        if (((SwtViewForm) form.getImpl()).separateTopCenter || (wHint != SWT.DEFAULT && leftSize.x + centerSize.x + rightSize.x > wHint)) {
            size.x = leftSize.x + rightSize.x;
            if (leftSize.x > 0 && rightSize.x > 0)
                size.x += form.horizontalSpacing;
            size.x = Math.max(centerSize.x, size.x);
            size.y = Math.max(leftSize.y, rightSize.y);
            if (center != null) {
                size.y += centerSize.y;
                if (left != null || right != null)
                    size.y += form.verticalSpacing;
            }
        } else {
            size.x = leftSize.x + centerSize.x + rightSize.x;
            int count = -1;
            if (leftSize.x > 0)
                count++;
            if (centerSize.x > 0)
                count++;
            if (rightSize.x > 0)
                count++;
            if (count > 0)
                size.x += count * form.horizontalSpacing;
            size.y = Math.max(leftSize.y, Math.max(centerSize.y, rightSize.y));
        }
        if (content != null) {
            // allow space for a vertical separator
            if (left != null || right != null || center != null)
                size.y += 1;
            Point contentSize = new Point(0, 0);
            contentSize = computeChildSize(content, SWT.DEFAULT, SWT.DEFAULT, flushCache);
            size.x = Math.max(size.x, contentSize.x);
            size.y += contentSize.y;
            if (size.y > contentSize.y)
                size.y += form.verticalSpacing;
        }
        size.x += 2 * form.marginWidth;
        size.y += 2 * form.marginHeight;
        if (wHint != SWT.DEFAULT)
            size.x = wHint;
        if (hHint != SWT.DEFAULT)
            size.y = hHint;
        return size;
    }

    Point computeChildSize(Control control, int wHint, int hHint, boolean flushCache) {
        Object data = control.getLayoutData();
        if (data == null || !(data instanceof CLayoutData)) {
            data = new CLayoutData();
            control.setLayoutData(data);
        }
        return ((CLayoutData) data).computeSize(control, wHint, hHint, flushCache);
    }

    int computeTrim(Control c) {
        if (c instanceof Scrollable) {
            Rectangle rect = ((Scrollable) c).computeTrim(0, 0, 0, 0);
            return rect.width;
        }
        return c.getBorderWidth() * 2;
    }

    @Override
    public boolean flushCache(Control control) {
        Object data = control.getLayoutData();
        if (data instanceof CLayoutData)
            ((CLayoutData) data).flushCache();
        return true;
    }

    @Override
    public void layout(Composite composite, boolean flushCache) {
        ViewForm form = (ViewForm) composite;
        Control left = ((SwtViewForm) form.getImpl()).topLeft;
        Control center = ((SwtViewForm) form.getImpl()).topCenter;
        Control right = ((SwtViewForm) form.getImpl()).topRight;
        Control content = ((SwtViewForm) form.getImpl()).content;
        Rectangle rect = composite.getClientArea();
        Point leftSize = new Point(0, 0);
        if (left != null && !left.isDisposed()) {
            leftSize = computeChildSize(left, SWT.DEFAULT, SWT.DEFAULT, flushCache);
        }
        Point centerSize = new Point(0, 0);
        if (center != null && !center.isDisposed()) {
            centerSize = computeChildSize(center, SWT.DEFAULT, SWT.DEFAULT, flushCache);
        }
        Point rightSize = new Point(0, 0);
        if (right != null && !right.isDisposed()) {
            rightSize = computeChildSize(right, SWT.DEFAULT, SWT.DEFAULT, flushCache);
        }
        int minTopWidth = leftSize.x + centerSize.x + rightSize.x + 2 * form.marginWidth + 2 * ((SwtViewForm) form.getImpl()).highlight;
        int count = -1;
        if (leftSize.x > 0)
            count++;
        if (centerSize.x > 0)
            count++;
        if (rightSize.x > 0)
            count++;
        if (count > 0)
            minTopWidth += count * form.horizontalSpacing;
        int x = rect.x + rect.width - form.marginWidth - ((SwtViewForm) form.getImpl()).highlight;
        int y = rect.y + form.marginHeight + ((SwtViewForm) form.getImpl()).highlight;
        boolean top = false;
        if (((SwtViewForm) form.getImpl()).separateTopCenter || minTopWidth > rect.width) {
            int topHeight = Math.max(rightSize.y, leftSize.y);
            if (right != null && !right.isDisposed()) {
                top = true;
                x -= rightSize.x;
                right.setBounds(x, y, rightSize.x, topHeight);
                x -= form.horizontalSpacing;
            }
            if (left != null && !left.isDisposed()) {
                top = true;
                int trim = computeTrim(left);
                int leftW = x - rect.x - form.marginWidth - ((SwtViewForm) form.getImpl()).highlight - trim;
                leftSize = computeChildSize(left, leftW, SWT.DEFAULT, false);
                left.setBounds(rect.x + form.marginWidth + ((SwtViewForm) form.getImpl()).highlight, y, leftSize.x, topHeight);
            }
            if (top)
                y += topHeight + form.verticalSpacing;
            if (center != null && !center.isDisposed()) {
                top = true;
                int trim = computeTrim(center);
                int w = rect.width - 2 * form.marginWidth - 2 * ((SwtViewForm) form.getImpl()).highlight - trim;
                Point size = computeChildSize(center, w, SWT.DEFAULT, false);
                if (size.x < centerSize.x) {
                    centerSize = size;
                }
                center.setBounds(rect.x + rect.width - form.marginWidth - ((SwtViewForm) form.getImpl()).highlight - centerSize.x, y, centerSize.x, centerSize.y);
                y += centerSize.y + form.verticalSpacing;
            }
        } else {
            int topHeight = Math.max(rightSize.y, Math.max(centerSize.y, leftSize.y));
            if (right != null && !right.isDisposed()) {
                top = true;
                x -= rightSize.x;
                right.setBounds(x, y, rightSize.x, topHeight);
                x -= form.horizontalSpacing;
            }
            if (center != null && !center.isDisposed()) {
                top = true;
                x -= centerSize.x;
                center.setBounds(x, y, centerSize.x, topHeight);
                x -= form.horizontalSpacing;
            }
            if (left != null && !left.isDisposed()) {
                top = true;
                Rectangle trim = left instanceof Composite ? ((Composite) left).computeTrim(0, 0, 0, 0) : new Rectangle(0, 0, 0, 0);
                int w = x - rect.x - form.marginWidth - ((SwtViewForm) form.getImpl()).highlight - trim.width;
                int h = topHeight - trim.height;
                leftSize = computeChildSize(left, w, h, false);
                left.setBounds(rect.x + form.marginWidth + ((SwtViewForm) form.getImpl()).highlight, y, leftSize.x, topHeight);
            }
            if (top)
                y += topHeight + form.verticalSpacing;
        }
        int oldSeperator = ((SwtViewForm) form.getImpl()).separator;
        ((SwtViewForm) form.getImpl()).separator = -1;
        if (content != null && !content.isDisposed()) {
            if (left != null || right != null || center != null) {
                ((SwtViewForm) form.getImpl()).separator = y;
                y++;
            }
            content.setBounds(rect.x + form.marginWidth + ((SwtViewForm) form.getImpl()).highlight, y, rect.width - 2 * form.marginWidth - 2 * ((SwtViewForm) form.getImpl()).highlight, rect.y + rect.height - y - form.marginHeight - ((SwtViewForm) form.getImpl()).highlight);
        }
        if (oldSeperator != ((SwtViewForm) form.getImpl()).separator) {
            int t, b;
            if (oldSeperator == -1) {
                t = ((SwtViewForm) form.getImpl()).separator;
                b = ((SwtViewForm) form.getImpl()).separator + 1;
            } else if (((SwtViewForm) form.getImpl()).separator == -1) {
                t = oldSeperator;
                b = oldSeperator + 1;
            } else {
                t = Math.min(((SwtViewForm) form.getImpl()).separator, oldSeperator);
                b = Math.max(((SwtViewForm) form.getImpl()).separator, oldSeperator);
            }
            form.redraw(((SwtViewForm) form.getImpl()).borderLeft, t, form.getSize().x - ((SwtViewForm) form.getImpl()).borderLeft - ((SwtViewForm) form.getImpl()).borderRight, b - t, false);
        }
    }
}
