/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2006 IBM Corporation and others.
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
 * This class provides the layout for CBanner
 *
 * @see CBanner
 */
class CBannerLayout extends SwtLayout {

    @Override
    public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        CBanner banner = (CBanner) composite;
        Control left = ((SwtCBanner) banner.getImpl()).left;
        Control right = ((SwtCBanner) banner.getImpl()).right;
        Control bottom = ((SwtCBanner) banner.getImpl()).bottom;
        boolean showCurve = left != null && right != null;
        int height = hHint;
        int width = wHint;
        // Calculate component sizes
        Point bottomSize = new Point(0, 0);
        if (bottom != null) {
            int trim = computeTrim(bottom);
            int w = wHint == SWT.DEFAULT ? SWT.DEFAULT : Math.max(0, width - trim);
            bottomSize = computeChildSize(bottom, w, SWT.DEFAULT, flushCache);
        }
        Point rightSize = new Point(0, 0);
        if (right != null) {
            int trim = computeTrim(right);
            int w = SWT.DEFAULT;
            if (((SwtCBanner) banner.getImpl()).rightWidth != SWT.DEFAULT) {
                w = ((SwtCBanner) banner.getImpl()).rightWidth - trim;
                if (left != null) {
                    w = Math.min(w, width - ((SwtCBanner) banner.getImpl()).curve_width + 2 * ((SwtCBanner) banner.getImpl()).curve_indent - SwtCBanner.MIN_LEFT - trim);
                }
                w = Math.max(0, w);
            }
            rightSize = computeChildSize(right, w, SWT.DEFAULT, flushCache);
            if (wHint != SWT.DEFAULT) {
                width -= rightSize.x + ((SwtCBanner) banner.getImpl()).curve_width - 2 * ((SwtCBanner) banner.getImpl()).curve_indent;
            }
        }
        Point leftSize = new Point(0, 0);
        if (left != null) {
            int trim = computeTrim(left);
            int w = wHint == SWT.DEFAULT ? SWT.DEFAULT : Math.max(0, width - trim);
            leftSize = computeChildSize(left, w, SWT.DEFAULT, flushCache);
        }
        // Add up sizes
        width = leftSize.x + rightSize.x;
        height = bottomSize.y;
        if (bottom != null && (left != null || right != null)) {
            height += SwtCBanner.BORDER_STRIPE + 2;
        }
        if (left != null) {
            if (right == null) {
                height += leftSize.y;
            } else {
                height += Math.max(leftSize.y, ((SwtCBanner) banner.getImpl()).rightMinHeight == SWT.DEFAULT ? rightSize.y : ((SwtCBanner) banner.getImpl()).rightMinHeight);
            }
        } else {
            height += rightSize.y;
        }
        if (showCurve) {
            width += ((SwtCBanner) banner.getImpl()).curve_width - 2 * ((SwtCBanner) banner.getImpl()).curve_indent;
            height += SwtCBanner.BORDER_TOP + SwtCBanner.BORDER_BOTTOM + 2 * SwtCBanner.BORDER_STRIPE;
        }
        if (wHint != SWT.DEFAULT)
            width = wHint;
        if (hHint != SWT.DEFAULT)
            height = hHint;
        return new Point(width, height);
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
        CBanner banner = (CBanner) composite;
        Control left = ((SwtCBanner) banner.getImpl()).left;
        Control right = ((SwtCBanner) banner.getImpl()).right;
        Control bottom = ((SwtCBanner) banner.getImpl()).bottom;
        Point size = banner.getSize();
        boolean showCurve = left != null && right != null;
        int width = size.x - 2 * banner.getBorderWidth();
        int height = size.y - 2 * banner.getBorderWidth();
        Point bottomSize = new Point(0, 0);
        if (bottom != null) {
            int trim = computeTrim(bottom);
            int w = Math.max(0, width - trim);
            bottomSize = computeChildSize(bottom, w, SWT.DEFAULT, flushCache);
            height -= bottomSize.y + SwtCBanner.BORDER_STRIPE + 2;
        }
        if (showCurve)
            height -= SwtCBanner.BORDER_TOP + SwtCBanner.BORDER_BOTTOM + 2 * SwtCBanner.BORDER_STRIPE;
        height = Math.max(0, height);
        Point rightSize = new Point(0, 0);
        if (right != null) {
            int trim = computeTrim(right);
            int w = SWT.DEFAULT;
            if (((SwtCBanner) banner.getImpl()).rightWidth != SWT.DEFAULT) {
                w = ((SwtCBanner) banner.getImpl()).rightWidth - trim;
                if (left != null) {
                    w = Math.min(w, width - ((SwtCBanner) banner.getImpl()).curve_width + 2 * ((SwtCBanner) banner.getImpl()).curve_indent - SwtCBanner.MIN_LEFT - trim);
                }
                w = Math.max(0, w);
            }
            rightSize = computeChildSize(right, w, SWT.DEFAULT, flushCache);
            width = width - (rightSize.x - ((SwtCBanner) banner.getImpl()).curve_indent + ((SwtCBanner) banner.getImpl()).curve_width - ((SwtCBanner) banner.getImpl()).curve_indent);
        }
        Point leftSize = new Point(0, 0);
        if (left != null) {
            int trim = computeTrim(left);
            int w = Math.max(0, width - trim);
            leftSize = computeChildSize(left, w, SWT.DEFAULT, flushCache);
        }
        int x = 0;
        int y = 0;
        int oldStart = ((SwtCBanner) banner.getImpl()).curveStart;
        Rectangle leftRect = null;
        Rectangle rightRect = null;
        Rectangle bottomRect = null;
        if (bottom != null) {
            bottomRect = new Rectangle(x, y + size.y - bottomSize.y, bottomSize.x, bottomSize.y);
        }
        if (showCurve)
            y += SwtCBanner.BORDER_TOP + SwtCBanner.BORDER_STRIPE;
        if (left != null) {
            leftRect = new Rectangle(x, y, leftSize.x, leftSize.y);
            ((SwtCBanner) banner.getImpl()).curveStart = x + leftSize.x - ((SwtCBanner) banner.getImpl()).curve_indent;
            x += leftSize.x - ((SwtCBanner) banner.getImpl()).curve_indent + ((SwtCBanner) banner.getImpl()).curve_width - ((SwtCBanner) banner.getImpl()).curve_indent;
        }
        if (right != null) {
            if (left != null) {
                rightSize.y = Math.max(leftSize.y, ((SwtCBanner) banner.getImpl()).rightMinHeight == SWT.DEFAULT ? rightSize.y : ((SwtCBanner) banner.getImpl()).rightMinHeight);
            }
            rightRect = new Rectangle(x, y, rightSize.x, rightSize.y);
        }
        if (((SwtCBanner) banner.getImpl()).curveStart < oldStart) {
            banner.redraw(((SwtCBanner) banner.getImpl()).curveStart - SwtCBanner.CURVE_TAIL, 0, oldStart + ((SwtCBanner) banner.getImpl()).curve_width - ((SwtCBanner) banner.getImpl()).curveStart + SwtCBanner.CURVE_TAIL + 5, size.y, false);
        }
        if (((SwtCBanner) banner.getImpl()).curveStart > oldStart) {
            banner.redraw(oldStart - SwtCBanner.CURVE_TAIL, 0, ((SwtCBanner) banner.getImpl()).curveStart + ((SwtCBanner) banner.getImpl()).curve_width - oldStart + SwtCBanner.CURVE_TAIL + 5, size.y, false);
        }
        /*
	 * The paint events must be flushed in order to make the curve draw smoothly
	 * while the user drags the divider.
	 * On Windows, it is necessary to flush the paints before the children are
	 * resized because otherwise the children (particularly toolbars) will flash.
	 */
        banner.update();
        ((SwtCBanner) banner.getImpl()).curveRect = new Rectangle(((SwtCBanner) banner.getImpl()).curveStart, 0, ((SwtCBanner) banner.getImpl()).curve_width, size.y);
        if (bottomRect != null)
            bottom.setBounds(bottomRect);
        if (rightRect != null)
            right.setBounds(rightRect);
        if (leftRect != null)
            left.setBounds(leftRect);
    }
}
