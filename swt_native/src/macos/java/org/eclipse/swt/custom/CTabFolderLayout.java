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
 * This class provides the layout for CTabFolder
 *
 * @see CTabFolder
 */
class CTabFolderLayout extends Layout {

    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        CTabFolder folder = (CTabFolder) composite;
        CTabItem[] items = ((SwtCTabFolder) folder.getImpl()).items;
        CTabFolderRenderer renderer = ((SwtCTabFolder) folder.getImpl()).renderer;
        // preferred width of tab area to show all tabs
        int tabW = 0;
        int selectedIndex = ((SwtCTabFolder) folder.getImpl()).selectedIndex;
        if (selectedIndex == -1)
            selectedIndex = 0;
        GC gc = new GC(folder);
        for (int i = 0; i < items.length; i++) {
            if (((SwtCTabFolder) folder.getImpl()).single) {
                tabW = Math.max(tabW, renderer.computeSize(i, SWT.SELECTED, gc, SWT.DEFAULT, SWT.DEFAULT).x);
            } else {
                int state = 0;
                if (i == selectedIndex)
                    state |= SWT.SELECTED;
                tabW += renderer.computeSize(i, state, gc, SWT.DEFAULT, SWT.DEFAULT).x;
            }
        }
        int width = 0, wrapHeight = 0;
        boolean leftControl = false, rightControl = false;
        if (wHint == SWT.DEFAULT) {
            for (int i = 0; i < ((SwtCTabFolder) folder.getImpl()).controls.length; i++) {
                Control control = ((SwtCTabFolder) folder.getImpl()).controls[i];
                if (!control.isDisposed() && control.getVisible()) {
                    if ((((SwtCTabFolder) folder.getImpl()).controlAlignments[i] & SWT.LEAD) != 0) {
                        leftControl = true;
                    } else {
                        rightControl = true;
                    }
                    width += control.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
                }
            }
        } else {
            Point size = new Point(wHint, hHint);
            boolean[][] positions = new boolean[1][];
            Rectangle[] rects = ((SwtCTabFolder) folder.getImpl()).computeControlBounds(size, positions);
            int minY = Integer.MAX_VALUE, maxY = 0;
            for (int i = 0; i < rects.length; i++) {
                if (positions[0][i]) {
                    minY = Math.min(minY, rects[i].y);
                    maxY = Math.max(maxY, rects[i].y + rects[i].height);
                    wrapHeight = maxY - minY;
                } else {
                    if ((((SwtCTabFolder) folder.getImpl()).controlAlignments[i] & SWT.LEAD) != 0) {
                        leftControl = true;
                    } else {
                        rightControl = true;
                    }
                    width += rects[i].width;
                }
            }
        }
        if (leftControl)
            width += SwtCTabFolder.SPACING * 2;
        if (rightControl)
            width += SwtCTabFolder.SPACING * 2;
        tabW += width;
        gc.dispose();
        int controlW = 0;
        int controlH = 0;
        // preferred size of controls in tab items
        for (CTabItem item : items) {
            Control control = ((SwtCTabItem) item.getImpl()).control;
            if (control != null && !control.isDisposed()) {
                Point size = control.computeSize(wHint, hHint, flushCache);
                controlW = Math.max(controlW, size.x);
                controlH = Math.max(controlH, size.y);
            }
        }
        int minWidth = Math.max(tabW, controlW + folder.marginWidth);
        int minHeight = (((SwtCTabFolder) folder.getImpl()).minimized) ? 0 : controlH + wrapHeight;
        if (minWidth == 0)
            minWidth = SwtCTabFolder.DEFAULT_WIDTH;
        if (minHeight == 0)
            minHeight = SwtCTabFolder.DEFAULT_HEIGHT;
        if (wHint != SWT.DEFAULT)
            minWidth = wHint;
        if (hHint != SWT.DEFAULT)
            minHeight = hHint;
        return new Point(minWidth, minHeight);
    }

    @Override
    protected boolean flushCache(Control control) {
        return true;
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
        CTabFolder folder = (CTabFolder) composite;
        // resize content
        if (((SwtCTabFolder) folder.getImpl()).selectedIndex != -1) {
            Control control = ((SwtCTabItem) ((SwtCTabFolder) folder.getImpl()).items[((SwtCTabFolder) folder.getImpl()).selectedIndex].getImpl()).control;
            if (control != null && !control.isDisposed()) {
                control.setBounds(folder.getClientArea());
            }
        }
    }
}
