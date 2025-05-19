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
class ScrolledCompositeLayout extends SwtLayout {

    boolean inLayout = false;

    static final int DEFAULT_WIDTH = 64;

    static final int DEFAULT_HEIGHT = 64;

    @Override
    public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        ScrolledComposite sc = (ScrolledComposite) composite;
        Point size = new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        if (((SwtScrolledComposite) sc.getImpl()).content != null) {
            Point preferredSize = ((SwtScrolledComposite) sc.getImpl()).content.computeSize(wHint, hHint, flushCache);
            Point currentSize = ((SwtScrolledComposite) sc.getImpl()).content.getSize();
            size.x = sc.getExpandHorizontal() ? preferredSize.x : currentSize.x;
            size.y = sc.getExpandVertical() ? preferredSize.y : currentSize.y;
        }
        size.x = Math.max(size.x, ((SwtScrolledComposite) sc.getImpl()).minWidth);
        size.y = Math.max(size.y, ((SwtScrolledComposite) sc.getImpl()).minHeight);
        if (wHint != SWT.DEFAULT)
            size.x = wHint;
        if (hHint != SWT.DEFAULT)
            size.y = hHint;
        return size;
    }

    @Override
    public boolean flushCache(Control control) {
        return true;
    }

    @Override
    public void layout(Composite composite, boolean flushCache) {
        if (inLayout)
            return;
        ScrolledComposite sc = (ScrolledComposite) composite;
        if (((SwtScrolledComposite) sc.getImpl()).content == null)
            return;
        ScrollBar hBar = sc.getHorizontalBar();
        ScrollBar vBar = sc.getVerticalBar();
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
        Rectangle contentRect = ((SwtScrolledComposite) sc.getImpl()).content.getBounds();
        if (!((SwtScrolledComposite) sc.getImpl()).alwaysShowScroll) {
            boolean hVisible = ((SwtScrolledComposite) sc.getImpl()).needHScroll(contentRect, false);
            boolean vVisible = ((SwtScrolledComposite) sc.getImpl()).needVScroll(contentRect, hVisible);
            if (!hVisible && vVisible)
                hVisible = ((SwtScrolledComposite) sc.getImpl()).needHScroll(contentRect, vVisible);
            if (hBar != null)
                hBar.setVisible(hVisible);
            if (vBar != null)
                vBar.setVisible(vVisible);
        }
        Rectangle hostRect = sc.getClientArea();
        if (((SwtScrolledComposite) sc.getImpl()).expandHorizontal) {
            contentRect.width = Math.max(((SwtScrolledComposite) sc.getImpl()).minWidth, hostRect.width);
        }
        if (((SwtScrolledComposite) sc.getImpl()).expandVertical) {
            contentRect.height = Math.max(((SwtScrolledComposite) sc.getImpl()).minHeight, hostRect.height);
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
        ((SwtScrolledComposite) sc.getImpl()).content.setBounds(contentRect);
        inLayout = false;
    }
}
