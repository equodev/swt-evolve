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
class CBannerLayout extends Layout {

    @Override
    protected Point computeSize(Composite composite_, int wHint, int hHint, boolean flushCache) {
        IComposite composite = (IComposite)composite_.delegate;
        SWTCBanner banner = (SWTCBanner) ((SWTCBanner) composite);
        SWTControl left = (SWTControl) (banner.getLeft());
        SWTControl right = (SWTControl) (banner.getRight());
        SWTControl bottom = (SWTControl) (banner.getBottom());
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
            if (banner.getRightWidth() != SWT.DEFAULT) {
                w = banner.getRightWidth() - trim;
                if (left != null) {
                    w = Math.min(w, width - banner.curve_width + 2 * banner.curve_indent - SWTCBanner.MIN_LEFT - trim);
                }
                w = Math.max(0, w);
            }
            rightSize = computeChildSize(right, w, SWT.DEFAULT, flushCache);
            if (wHint != SWT.DEFAULT) {
                width -= rightSize.x + banner.curve_width - 2 * banner.curve_indent;
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
            height += SWTCBanner.BORDER_STRIPE + 2;
        }
        if (left != null) {
            if (right == null) {
                height += leftSize.y;
            } else {
                height += Math.max(leftSize.y, banner.rightMinHeight == SWT.DEFAULT ? rightSize.y : banner.rightMinHeight);
            }
        } else {
            height += rightSize.y;
        }
        if (showCurve) {
            width += banner.curve_width - 2 * banner.curve_indent;
            height += SWTCBanner.BORDER_TOP + SWTCBanner.BORDER_BOTTOM + 2 * SWTCBanner.BORDER_STRIPE;
        }
        if (wHint != SWT.DEFAULT)
            width = wHint;
        if (hHint != SWT.DEFAULT)
            height = hHint;
        return new Point(width, height);
    }

    Point computeChildSize(IControl control, int wHint, int hHint, boolean flushCache) {
        Object data = control.getLayoutData();
        if (data == null || !(data instanceof CLayoutData)) {
            data = new CLayoutData();
            control.setLayoutData(data);
        }
        return ((CLayoutData) data).computeSize(control, wHint, hHint, flushCache);
    }

    int computeTrim(IControl c) {
        if (c instanceof SWTScrollable) {
            Rectangle rect = ((SWTScrollable) c).computeTrim(0, 0, 0, 0);
            return rect.width;
        }
        return c.getBorderWidth() * 2;
    }

    @Override
    protected boolean flushCache(Control control) {
        Object data = control.getLayoutData();
        if (data instanceof CLayoutData)
            ((CLayoutData) data).flushCache();
        return true;
    }

    @Override
    protected void layout(Composite composite_, boolean flushCache) {
        IComposite composite = (IComposite)composite_.delegate;
        SWTCBanner banner = (SWTCBanner) ((SWTCBanner) composite);
        SWTControl left = (SWTControl) (banner.getLeft());
        SWTControl right = (SWTControl) (banner.getRight());
        SWTControl bottom = (SWTControl) (banner.getBottom());
        Point size = banner.getSize();
        boolean showCurve = left != null && right != null;
        int width = size.x - 2 * banner.getBorderWidth();
        int height = size.y - 2 * banner.getBorderWidth();
        Point bottomSize = new Point(0, 0);
        if (bottom != null) {
            int trim = computeTrim(bottom);
            int w = Math.max(0, width - trim);
            bottomSize = computeChildSize(bottom, w, SWT.DEFAULT, flushCache);
            height -= bottomSize.y + SWTCBanner.BORDER_STRIPE + 2;
        }
        if (showCurve)
            height -= SWTCBanner.BORDER_TOP + SWTCBanner.BORDER_BOTTOM + 2 * SWTCBanner.BORDER_STRIPE;
        height = Math.max(0, height);
        Point rightSize = new Point(0, 0);
        if (right != null) {
            int trim = computeTrim(right);
            int w = SWT.DEFAULT;
            if (banner.getRightWidth() != SWT.DEFAULT) {
                w = banner.getRightWidth() - trim;
                if (left != null) {
                    w = Math.min(w, width - banner.curve_width + 2 * banner.curve_indent - SWTCBanner.MIN_LEFT - trim);
                }
                w = Math.max(0, w);
            }
            rightSize = computeChildSize(right, w, SWT.DEFAULT, flushCache);
            width = width - (rightSize.x - banner.curve_indent + banner.curve_width - banner.curve_indent);
        }
        Point leftSize = new Point(0, 0);
        if (left != null) {
            int trim = computeTrim(left);
            int w = Math.max(0, width - trim);
            leftSize = computeChildSize(left, w, SWT.DEFAULT, flushCache);
        }
        int x = 0;
        int y = 0;
        int oldStart = banner.curveStart;
        Rectangle leftRect = null;
        Rectangle rightRect = null;
        Rectangle bottomRect = null;
        if (bottom != null) {
            bottomRect = new Rectangle(x, y + size.y - bottomSize.y, bottomSize.x, bottomSize.y);
        }
        if (showCurve)
            y += SWTCBanner.BORDER_TOP + SWTCBanner.BORDER_STRIPE;
        if (left != null) {
            leftRect = new Rectangle(x, y, leftSize.x, leftSize.y);
            banner.curveStart = x + leftSize.x - banner.curve_indent;
            x += leftSize.x - banner.curve_indent + banner.curve_width - banner.curve_indent;
        }
        if (right != null) {
            if (left != null) {
                rightSize.y = Math.max(leftSize.y, banner.rightMinHeight == SWT.DEFAULT ? rightSize.y : banner.rightMinHeight);
            }
            rightRect = new Rectangle(x, y, rightSize.x, rightSize.y);
        }
        if (banner.curveStart < oldStart) {
            banner.redraw(banner.curveStart - SWTCBanner.CURVE_TAIL, 0, oldStart + banner.curve_width - banner.curveStart + SWTCBanner.CURVE_TAIL + 5, size.y, false);
        }
        if (banner.curveStart > oldStart) {
            banner.redraw(oldStart - SWTCBanner.CURVE_TAIL, 0, banner.curveStart + banner.curve_width - oldStart + SWTCBanner.CURVE_TAIL + 5, size.y, false);
        }
        /*
	 * The paint events must be flushed in order to make the curve draw smoothly
	 * while the user drags the divider.
	 * On Windows, it is necessary to flush the paints before the children are
	 * resized because otherwise the children (particularly toolbars) will flash.
	 */
        banner.update();
        banner.curveRect = new Rectangle(banner.curveStart, 0, banner.curve_width, size.y);
        if (bottomRect != null)
            bottom.setBounds(bottomRect);
        if (rightRect != null)
            right.setBounds(rightRect);
        if (leftRect != null)
            left.setBounds(leftRect);
    }
}
