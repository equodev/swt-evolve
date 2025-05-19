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
 * This class provides the layout for SashForm
 *
 * @see SashForm
 */
class SashFormLayout extends SwtLayout {

    @Override
    public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        SashForm sashForm = (SashForm) composite;
        Control[] cArray = ((SwtSashForm) sashForm.getImpl()).getControls(true);
        int width = 0;
        int height = 0;
        if (cArray.length == 0) {
            if (wHint != SWT.DEFAULT)
                width = wHint;
            if (hHint != SWT.DEFAULT)
                height = hHint;
            return new Point(width, height);
        }
        // determine control sizes
        boolean vertical = sashForm.getOrientation() == SWT.VERTICAL;
        int maxIndex = 0;
        int maxValue = 0;
        for (int i = 0; i < cArray.length; i++) {
            if (vertical) {
                Point size = cArray[i].computeSize(wHint, SWT.DEFAULT, flushCache);
                if (size.y > maxValue) {
                    maxIndex = i;
                    maxValue = size.y;
                }
                width = Math.max(width, size.x);
            } else {
                Point size = cArray[i].computeSize(SWT.DEFAULT, hHint, flushCache);
                if (size.x > maxValue) {
                    maxIndex = i;
                    maxValue = size.x;
                }
                height = Math.max(height, size.y);
            }
        }
        // get the ratios
        long[] ratios = new long[cArray.length];
        long total = 0;
        for (int i = 0; i < cArray.length; i++) {
            Object data = cArray[i].getLayoutData();
            if (data instanceof SashFormData) {
                ratios[i] = ((SashFormData) data).weight;
            } else {
                data = new SashFormData();
                cArray[i].setLayoutData(data);
                ((SashFormData) data).weight = ratios[i] = ((200 << 16) + 999) / 1000;
            }
            total += ratios[i];
        }
        if (ratios[maxIndex] > 0) {
            int sashwidth = ((SwtSashForm) sashForm.getImpl()).sashes.length > 0 ? sashForm.SASH_WIDTH + ((SwtSashForm) sashForm.getImpl()).sashes[0].getBorderWidth() * 2 : sashForm.SASH_WIDTH;
            if (vertical) {
                height += (int) (total * maxValue / ratios[maxIndex]) + (cArray.length - 1) * sashwidth;
            } else {
                width += (int) (total * maxValue / ratios[maxIndex]) + (cArray.length - 1) * sashwidth;
            }
        }
        width += sashForm.getBorderWidth() * 2;
        height += sashForm.getBorderWidth() * 2;
        if (wHint != SWT.DEFAULT)
            width = wHint;
        if (hHint != SWT.DEFAULT)
            height = hHint;
        return new Point(width, height);
    }

    @Override
    public boolean flushCache(Control control) {
        return true;
    }

    @Override
    public void layout(Composite composite, boolean flushCache) {
        SashForm sashForm = (SashForm) composite;
        Rectangle area = sashForm.getClientArea();
        if (area.width <= 1 || area.height <= 1)
            return;
        Control[] newControls = ((SwtSashForm) sashForm.getImpl()).getControls(true);
        if (((SwtSashForm) sashForm.getImpl()).controls.length == 0 && newControls.length == 0)
            return;
        ((SwtSashForm) sashForm.getImpl()).controls = newControls;
        Control[] controls = ((SwtSashForm) sashForm.getImpl()).controls;
        if (((SwtSashForm) sashForm.getImpl()).maxControl != null && !((SwtSashForm) sashForm.getImpl()).maxControl.isDisposed()) {
            for (Control control : controls) {
                if (control != ((SwtSashForm) sashForm.getImpl()).maxControl) {
                    control.setBounds(-200, -200, 0, 0);
                } else {
                    control.setBounds(area);
                }
            }
            return;
        }
        // keep just the right number of sashes
        if (((SwtSashForm) sashForm.getImpl()).sashes.length < controls.length - 1) {
            Sash[] newSashes = new Sash[controls.length - 1];
            System.arraycopy(((SwtSashForm) sashForm.getImpl()).sashes, 0, newSashes, 0, ((SwtSashForm) sashForm.getImpl()).sashes.length);
            for (int i = ((SwtSashForm) sashForm.getImpl()).sashes.length; i < newSashes.length; i++) {
                newSashes[i] = ((SwtSashForm) sashForm.getImpl()).createSash();
            }
            ((SwtSashForm) sashForm.getImpl()).sashes = newSashes;
        }
        if (((SwtSashForm) sashForm.getImpl()).sashes.length > controls.length - 1) {
            if (controls.length == 0) {
                for (Sash sash : ((SwtSashForm) sashForm.getImpl()).sashes) {
                    sash.dispose();
                }
                ((SwtSashForm) sashForm.getImpl()).sashes = new Sash[0];
            } else {
                Sash[] newSashes = new Sash[controls.length - 1];
                System.arraycopy(((SwtSashForm) sashForm.getImpl()).sashes, 0, newSashes, 0, newSashes.length);
                for (int i = controls.length - 1; i < ((SwtSashForm) sashForm.getImpl()).sashes.length; i++) {
                    ((SwtSashForm) sashForm.getImpl()).sashes[i].dispose();
                }
                ((SwtSashForm) sashForm.getImpl()).sashes = newSashes;
            }
        }
        if (controls.length == 0)
            return;
        Sash[] sashes = ((SwtSashForm) sashForm.getImpl()).sashes;
        // get the ratios
        long[] ratios = new long[controls.length];
        long total = 0;
        for (int i = 0; i < controls.length; i++) {
            Object data = controls[i].getLayoutData();
            if (data instanceof SashFormData) {
                ratios[i] = ((SashFormData) data).weight;
            } else {
                data = new SashFormData();
                controls[i].setLayoutData(data);
                ((SashFormData) data).weight = ratios[i] = ((200 << 16) + 999) / 1000;
            }
            total += ratios[i];
        }
        int sashwidth = sashes.length > 0 ? sashForm.SASH_WIDTH + sashes[0].getBorderWidth() * 2 : sashForm.SASH_WIDTH;
        if (sashForm.getOrientation() == SWT.HORIZONTAL) {
            int width = (int) (ratios[0] * (area.width - sashes.length * sashwidth) / total);
            int x = area.x;
            controls[0].setBounds(x, area.y, width, area.height);
            x += width;
            for (int i = 1; i < controls.length - 1; i++) {
                sashes[i - 1].setBounds(x, area.y, sashwidth, area.height);
                x += sashwidth;
                width = (int) (ratios[i] * (area.width - sashes.length * sashwidth) / total);
                controls[i].setBounds(x, area.y, width, area.height);
                x += width;
            }
            if (controls.length > 1) {
                sashes[sashes.length - 1].setBounds(x, area.y, sashwidth, area.height);
                x += sashwidth;
                width = area.width - x;
                controls[controls.length - 1].setBounds(x, area.y, width, area.height);
            }
        } else {
            int height = (int) (ratios[0] * (area.height - sashes.length * sashwidth) / total);
            int y = area.y;
            controls[0].setBounds(area.x, y, area.width, height);
            y += height;
            for (int i = 1; i < controls.length - 1; i++) {
                sashes[i - 1].setBounds(area.x, y, area.width, sashwidth);
                y += sashwidth;
                height = (int) (ratios[i] * (area.height - sashes.length * sashwidth) / total);
                controls[i].setBounds(area.x, y, area.width, height);
                y += height;
            }
            if (controls.length > 1) {
                sashes[sashes.length - 1].setBounds(area.x, y, area.width, sashwidth);
                y += sashwidth;
                height = area.height - y;
                controls[controls.length - 1].setBounds(area.x, y, area.width, height);
            }
        }
    }
}
