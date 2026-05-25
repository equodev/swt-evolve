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
import dev.equo.swt.*;

/**
 * This class provides the layout for ScrolledComposite
 *
 * @see ScrolledComposite
 */
class DartScrolledCompositeLayout extends DartLayout implements IScrolledCompositeLayout {

    boolean inLayout = false;

    static final int DEFAULT_WIDTH = 64;

    static final int DEFAULT_HEIGHT = 64;

    @Override
    public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        ScrolledComposite sc = (ScrolledComposite) composite;
        Point size = new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        if (((DartScrolledComposite) sc.getImpl()).content != null) {
            Point preferredSize = ((DartScrolledComposite) sc.getImpl()).content.computeSize(wHint, hHint, flushCache);
            Point currentSize = ((DartScrolledComposite) sc.getImpl()).content.getSize();
            size.x = sc.getExpandHorizontal() ? preferredSize.x : currentSize.x;
            size.y = sc.getExpandVertical() ? preferredSize.y : currentSize.y;
        }
        size.x = Math.max(size.x, ((DartScrolledComposite) sc.getImpl()).minWidth);
        size.y = Math.max(size.y, ((DartScrolledComposite) sc.getImpl()).minHeight);
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
        if (((DartScrolledComposite) sc.getImpl()).content == null)
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
        Rectangle contentRect = ((DartScrolledComposite) sc.getImpl()).content.getBounds();
        if (!((DartScrolledComposite) sc.getImpl()).alwaysShowScroll) {
            boolean hVisible = sc.getImpl().needHScroll(contentRect, false);
            boolean vVisible = sc.getImpl().needVScroll(contentRect, hVisible);
            if (!hVisible && vVisible)
                hVisible = sc.getImpl().needHScroll(contentRect, vVisible);
            if (hBar != null)
                hBar.setVisible(hVisible);
            if (vBar != null)
                vBar.setVisible(vVisible);
        }
        Rectangle hostRect = sc.getClientArea();
        if (((DartScrolledComposite) sc.getImpl()).expandHorizontal) {
            contentRect.width = Math.max(((DartScrolledComposite) sc.getImpl()).minWidth, hostRect.width);
        }
        if (((DartScrolledComposite) sc.getImpl()).expandVertical) {
            contentRect.height = Math.max(((DartScrolledComposite) sc.getImpl()).minHeight, hostRect.height);
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
        ((DartScrolledComposite) sc.getImpl()).content.setBounds(contentRect);
        inLayout = false;
    }

    public boolean _inLayout() {
        return inLayout;
    }

    public ScrolledCompositeLayout getApi() {
        if (api == null)
            api = ScrolledCompositeLayout.createApi(this);
        return (ScrolledCompositeLayout) api;
    }

    public DartScrolledCompositeLayout(ScrolledCompositeLayout api) {
        super(api);
    }
}
