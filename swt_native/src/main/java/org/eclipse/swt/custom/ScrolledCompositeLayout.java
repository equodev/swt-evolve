/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
 * This class provides the layout for ScrolledComposite
 *
 * @see ScrolledComposite
 */
class ScrolledCompositeLayout extends Layout {

    boolean inLayout = false;

    static final int DEFAULT_WIDTH = 64;

    static final int DEFAULT_HEIGHT = 64;

    @Override
    protected Point computeSize(IComposite composite, int wHint, int hHint, boolean flushCache) {
        SWTScrolledComposite sc = (SWTScrolledComposite) ((SWTScrolledComposite) composite);
        Point size = new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        if (sc.getContent() != null) {
            Point preferredSize = sc.getContent().computeSize(wHint, hHint, flushCache);
            Point currentSize = sc.getContent().getSize();
            size.x = sc.getExpandHorizontal() ? preferredSize.x : currentSize.x;
            size.y = sc.getExpandVertical() ? preferredSize.y : currentSize.y;
        }
        size.x = Math.max(size.x, sc.getMinWidth());
        size.y = Math.max(size.y, sc.getMinHeight());
        if (wHint != SWT.DEFAULT)
            size.x = wHint;
        if (hHint != SWT.DEFAULT)
            size.y = hHint;
        return size;
    }

    @Override
    protected boolean flushCache(IControl control) {
        return true;
    }

    @Override
    protected void layout(IComposite composite, boolean flushCache) {
        if (inLayout)
            return;
        SWTScrolledComposite sc = (SWTScrolledComposite) ((SWTScrolledComposite) composite);
        if (sc.getContent() == null)
            return;
        SWTScrollBar hBar = (SWTScrollBar) (((SWTScrollBar) (sc.getHorizontalBar())));
        SWTScrollBar vBar = (SWTScrollBar) (((SWTScrollBar) (sc.getVerticalBar())));
        if (hBar != null) {
            if (hBar.getSize().y >= sc.getSize().y) {
                return;
            }
        }
        if (vBar != null) {
            if (vBar.getSize().x >= sc.getSize().x) {
                return;
            }
        }
        inLayout = true;
        Rectangle contentRect = sc.getContent().getBounds();
        if (!sc.getAlwaysShowScrollBars()) {
            boolean hVisible = sc.needHScroll(contentRect, false);
            boolean vVisible = sc.needVScroll(contentRect, hVisible);
            if (!hVisible && vVisible)
                hVisible = sc.needHScroll(contentRect, vVisible);
            if (hBar != null)
                hBar.setVisible(hVisible);
            if (vBar != null)
                vBar.setVisible(vVisible);
        }
        Rectangle hostRect = sc.getClientArea();
        if (sc.getExpandHorizontal()) {
            contentRect.width = Math.max(sc.getMinWidth(), hostRect.width);
        }
        if (sc.getExpandVertical()) {
            contentRect.height = Math.max(sc.getMinHeight(), hostRect.height);
        }
        GC gc = new GC(sc);
        if (hBar != null) {
            hBar.setMaximum(contentRect.width);
            hBar.setThumb(Math.min(contentRect.width, hostRect.width));
            hBar.setIncrement((int) gc.getFontMetrics().getAverageCharacterWidth());
            hBar.setPageIncrement(hostRect.width);
            int hPage = contentRect.width - hostRect.width;
            int hSelection = hBar.getSelection();
            if (hSelection >= hPage) {
                if (hPage <= 0) {
                    hSelection = 0;
                    hBar.setSelection(0);
                }
                contentRect.x = -hSelection;
            }
        }
        if (vBar != null) {
            vBar.setMaximum(contentRect.height);
            vBar.setThumb(Math.min(contentRect.height, hostRect.height));
            vBar.setIncrement(gc.getFontMetrics().getHeight());
            vBar.setPageIncrement(hostRect.height);
            int vPage = contentRect.height - hostRect.height;
            int vSelection = vBar.getSelection();
            if (vSelection >= vPage) {
                if (vPage <= 0) {
                    vSelection = 0;
                    vBar.setSelection(0);
                }
                contentRect.y = -vSelection;
            }
        }
        gc.dispose();
        sc.getContent().setBounds(contentRect);
        inLayout = false;
    }
}
