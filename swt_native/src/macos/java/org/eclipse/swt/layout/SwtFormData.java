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
 * Instances of this class are used to define the attachments
 * of a control in a <code>FormLayout</code>.
 * <p>
 * To set a <code>FormData</code> object into a control, you use the
 * <code>setLayoutData ()</code> method. To define attachments for the
 * <code>FormData</code>, set the fields directly, like this:
 * </p>
 * <pre>
 * 		FormData data = new FormData();
 * 		data.left = new FormAttachment(0,5);
 * 		data.right = new FormAttachment(100,-5);
 * 		button.setLayoutData(formData);
 * </pre>
 * <p>
 * <code>FormData</code> contains the <code>FormAttachments</code> for
 * each edge of the control that the <code>FormLayout</code> uses to
 * determine the size and position of the control. <code>FormData</code>
 * objects also allow you to set the width and height of controls within
 * a <code>FormLayout</code>.
 * </p>
 *
 * @see FormLayout
 * @see FormAttachment
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 2.0
 */
public final class SwtFormData implements IFormData {

    int cacheWidth = -1, cacheHeight = -1;

    int defaultWhint, defaultHhint, defaultWidth = -1, defaultHeight = -1;

    int currentWhint, currentHhint, currentWidth = -1, currentHeight = -1;

    FormAttachment cacheLeft, cacheRight, cacheTop, cacheBottom;

    boolean isVisited, needed;

    /**
     * Constructs a new instance of FormData using
     * default values.
     */
    public SwtFormData() {
    }

    /**
     * Constructs a new instance of FormData according to the parameters.
     * A value of SWT.DEFAULT indicates that no minimum width or
     * no minimum height is specified.
     *
     * @param width a minimum width for the control
     * @param height a minimum height for the control
     */
    public SwtFormData(int width, int height) {
        this.getApi().width = width;
        this.getApi().height = height;
    }

    void computeSize(Control control, int wHint, int hHint, boolean flushCache) {
        if (cacheWidth != -1 && cacheHeight != -1)
            return;
        if (wHint == this.getApi().width && hHint == this.getApi().height) {
            if (defaultWidth == -1 || defaultHeight == -1 || wHint != defaultWhint || hHint != defaultHhint) {
                Point size = control.computeSize(wHint, hHint, flushCache);
                defaultWhint = wHint;
                defaultHhint = hHint;
                defaultWidth = size.x;
                defaultHeight = size.y;
            }
            cacheWidth = defaultWidth;
            cacheHeight = defaultHeight;
            return;
        }
        if (currentWidth == -1 || currentHeight == -1 || wHint != currentWhint || hHint != currentHhint) {
            Point size = control.computeSize(wHint, hHint, flushCache);
            currentWhint = wHint;
            currentHhint = hHint;
            currentWidth = size.x;
            currentHeight = size.y;
        }
        cacheWidth = currentWidth;
        cacheHeight = currentHeight;
    }

    void flushCache() {
        cacheWidth = cacheHeight = -1;
        defaultHeight = defaultWidth = -1;
        currentHeight = currentWidth = -1;
    }

    int getWidth(Control control, boolean flushCache) {
        needed = true;
        computeSize(control, getApi().width, getApi().height, flushCache);
        return cacheWidth;
    }

    int getHeight(Control control, boolean flushCache) {
        computeSize(control, getApi().width, getApi().height, flushCache);
        return cacheHeight;
    }

    FormAttachment getBottomAttachment(Control control, int spacing, boolean flushCache) {
        if (cacheBottom != null)
            return cacheBottom;
        if (isVisited)
            return cacheBottom = new FormAttachment(0, getHeight(control, flushCache));
        if (getApi().bottom == null) {
            if (getApi().top == null)
                return cacheBottom = new FormAttachment(0, getHeight(control, flushCache));
            return cacheBottom = ((SwtFormAttachment) getTopAttachment(control, spacing, flushCache).getImpl()).plus(getHeight(control, flushCache));
        }
        Control bottomControl = getApi().bottom.control;
        if (bottomControl != null) {
            if (bottomControl.isDisposed()) {
                getApi().bottom.control = bottomControl = null;
            } else {
                if (bottomControl.getParent() != control.getParent()) {
                    bottomControl = null;
                }
            }
        }
        if (bottomControl == null)
            return cacheBottom = getApi().bottom;
        isVisited = true;
        FormData bottomData = (FormData) bottomControl.getLayoutData();
        FormAttachment bottomAttachment = ((SwtFormData) bottomData.getImpl()).getBottomAttachment(bottomControl, spacing, flushCache);
        switch(getApi().bottom.alignment) {
            case SWT.BOTTOM:
                cacheBottom = ((SwtFormAttachment) bottomAttachment.getImpl()).plus(getApi().bottom.offset);
                break;
            case SWT.CENTER:
                {
                    FormAttachment topAttachment = ((SwtFormData) bottomData.getImpl()).getTopAttachment(bottomControl, spacing, flushCache);
                    FormAttachment bottomHeight = ((SwtFormAttachment) bottomAttachment.getImpl()).minus(topAttachment);
                    cacheBottom = ((SwtFormAttachment) bottomAttachment.getImpl()).minus(((SwtFormAttachment) bottomHeight.minus(getHeight(control, flushCache)).getImpl()).divide(2));
                    break;
                }
            default:
                {
                    FormAttachment topAttachment = ((SwtFormData) bottomData.getImpl()).getTopAttachment(bottomControl, spacing, flushCache);
                    cacheBottom = ((SwtFormAttachment) topAttachment.getImpl()).plus(getApi().bottom.offset - spacing);
                    break;
                }
        }
        isVisited = false;
        return cacheBottom;
    }

    FormAttachment getLeftAttachment(Control control, int spacing, boolean flushCache) {
        if (cacheLeft != null)
            return cacheLeft;
        if (isVisited)
            return cacheLeft = new FormAttachment(0, 0);
        if (getApi().left == null) {
            if (getApi().right == null)
                return cacheLeft = new FormAttachment(0, 0);
            return cacheLeft = ((SwtFormAttachment) getRightAttachment(control, spacing, flushCache).getImpl()).minus(getWidth(control, flushCache));
        }
        Control leftControl = getApi().left.control;
        if (leftControl != null) {
            if (leftControl.isDisposed()) {
                getApi().left.control = leftControl = null;
            } else {
                if (leftControl.getParent() != control.getParent()) {
                    leftControl = null;
                }
            }
        }
        if (leftControl == null)
            return cacheLeft = getApi().left;
        isVisited = true;
        FormData leftData = (FormData) leftControl.getLayoutData();
        FormAttachment leftAttachment = ((SwtFormData) leftData.getImpl()).getLeftAttachment(leftControl, spacing, flushCache);
        switch(getApi().left.alignment) {
            case SWT.LEFT:
                cacheLeft = ((SwtFormAttachment) leftAttachment.getImpl()).plus(getApi().left.offset);
                break;
            case SWT.CENTER:
                {
                    FormAttachment rightAttachment = ((SwtFormData) leftData.getImpl()).getRightAttachment(leftControl, spacing, flushCache);
                    FormAttachment leftWidth = ((SwtFormAttachment) rightAttachment.getImpl()).minus(leftAttachment);
                    cacheLeft = ((SwtFormAttachment) leftAttachment.getImpl()).plus(((SwtFormAttachment) leftWidth.minus(getWidth(control, flushCache)).getImpl()).divide(2));
                    break;
                }
            default:
                {
                    FormAttachment rightAttachment = ((SwtFormData) leftData.getImpl()).getRightAttachment(leftControl, spacing, flushCache);
                    cacheLeft = ((SwtFormAttachment) rightAttachment.getImpl()).plus(getApi().left.offset + spacing);
                }
        }
        isVisited = false;
        return cacheLeft;
    }

    String getName() {
        String string = getClass().getName();
        int index = string.lastIndexOf('.');
        if (index == -1)
            return string;
        return string.substring(index + 1, string.length());
    }

    FormAttachment getRightAttachment(Control control, int spacing, boolean flushCache) {
        if (cacheRight != null)
            return cacheRight;
        if (isVisited)
            return cacheRight = new FormAttachment(0, getWidth(control, flushCache));
        if (getApi().right == null) {
            if (getApi().left == null)
                return cacheRight = new FormAttachment(0, getWidth(control, flushCache));
            return cacheRight = ((SwtFormAttachment) getLeftAttachment(control, spacing, flushCache).getImpl()).plus(getWidth(control, flushCache));
        }
        Control rightControl = getApi().right.control;
        if (rightControl != null) {
            if (rightControl.isDisposed()) {
                getApi().right.control = rightControl = null;
            } else {
                if (rightControl.getParent() != control.getParent()) {
                    rightControl = null;
                }
            }
        }
        if (rightControl == null)
            return cacheRight = getApi().right;
        isVisited = true;
        FormData rightData = (FormData) rightControl.getLayoutData();
        FormAttachment rightAttachment = ((SwtFormData) rightData.getImpl()).getRightAttachment(rightControl, spacing, flushCache);
        switch(getApi().right.alignment) {
            case SWT.RIGHT:
                cacheRight = ((SwtFormAttachment) rightAttachment.getImpl()).plus(getApi().right.offset);
                break;
            case SWT.CENTER:
                {
                    FormAttachment leftAttachment = ((SwtFormData) rightData.getImpl()).getLeftAttachment(rightControl, spacing, flushCache);
                    FormAttachment rightWidth = ((SwtFormAttachment) rightAttachment.getImpl()).minus(leftAttachment);
                    cacheRight = ((SwtFormAttachment) rightAttachment.getImpl()).minus(((SwtFormAttachment) rightWidth.minus(getWidth(control, flushCache)).getImpl()).divide(2));
                    break;
                }
            default:
                {
                    FormAttachment leftAttachment = ((SwtFormData) rightData.getImpl()).getLeftAttachment(rightControl, spacing, flushCache);
                    cacheRight = ((SwtFormAttachment) leftAttachment.getImpl()).plus(getApi().right.offset - spacing);
                    break;
                }
        }
        isVisited = false;
        return cacheRight;
    }

    FormAttachment getTopAttachment(Control control, int spacing, boolean flushCache) {
        if (cacheTop != null)
            return cacheTop;
        if (isVisited)
            return cacheTop = new FormAttachment(0, 0);
        if (getApi().top == null) {
            if (getApi().bottom == null)
                return cacheTop = new FormAttachment(0, 0);
            return cacheTop = ((SwtFormAttachment) getBottomAttachment(control, spacing, flushCache).getImpl()).minus(getHeight(control, flushCache));
        }
        Control topControl = getApi().top.control;
        if (topControl != null) {
            if (topControl.isDisposed()) {
                getApi().top.control = topControl = null;
            } else {
                if (topControl.getParent() != control.getParent()) {
                    topControl = null;
                }
            }
        }
        if (topControl == null)
            return cacheTop = getApi().top;
        isVisited = true;
        FormData topData = (FormData) topControl.getLayoutData();
        FormAttachment topAttachment = ((SwtFormData) topData.getImpl()).getTopAttachment(topControl, spacing, flushCache);
        switch(getApi().top.alignment) {
            case SWT.TOP:
                cacheTop = ((SwtFormAttachment) topAttachment.getImpl()).plus(getApi().top.offset);
                break;
            case SWT.CENTER:
                {
                    FormAttachment bottomAttachment = ((SwtFormData) topData.getImpl()).getBottomAttachment(topControl, spacing, flushCache);
                    FormAttachment topHeight = ((SwtFormAttachment) bottomAttachment.getImpl()).minus(topAttachment);
                    cacheTop = ((SwtFormAttachment) topAttachment.getImpl()).plus(((SwtFormAttachment) topHeight.minus(getHeight(control, flushCache)).getImpl()).divide(2));
                    break;
                }
            default:
                {
                    FormAttachment bottomAttachment = ((SwtFormData) topData.getImpl()).getBottomAttachment(topControl, spacing, flushCache);
                    cacheTop = ((SwtFormAttachment) bottomAttachment.getImpl()).plus(getApi().top.offset + spacing);
                    break;
                }
        }
        isVisited = false;
        return cacheTop;
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the FormData object
     */
    @Override
    public String toString() {
        String string = getName() + " {";
        if (getApi().width != SWT.DEFAULT)
            string += "width=" + getApi().width + " ";
        if (getApi().height != SWT.DEFAULT)
            string += "height=" + getApi().height + " ";
        if (getApi().left != null)
            string += "left=" + getApi().left + " ";
        if (getApi().right != null)
            string += "right=" + getApi().right + " ";
        if (getApi().top != null)
            string += "top=" + getApi().top + " ";
        if (getApi().bottom != null)
            string += "bottom=" + getApi().bottom + " ";
        string = string.trim();
        string += "}";
        return string;
    }

    public FormData getApi() {
        if (api == null)
            api = FormData.createApi(this);
        return (FormData) api;
    }

    protected FormData api;

    public void setApi(FormData api) {
        this.api = api;
    }
}
