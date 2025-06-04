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
 * <code>GridData</code> is the layout data object associated with
 * <code>GridLayout</code>. To set a <code>GridData</code> object into a
 * control, you use the <code>Control.setLayoutData(Object)</code> method.
 * <p>
 * There are two ways to create a <code>GridData</code> object with certain
 * fields set. The first is to set the fields directly, like this:</p>
 * <pre>
 * 		GridData gridData = new GridData();
 * 		gridData.horizontalAlignment = GridData.FILL;
 * 		gridData.grabExcessHorizontalSpace = true;
 * 		button1.setLayoutData(gridData);
 *
 * 		gridData = new GridData();
 * 		gridData.horizontalAlignment = GridData.FILL;
 * 		gridData.verticalAlignment = GridData.FILL;
 * 		gridData.grabExcessHorizontalSpace = true;
 * 		gridData.grabExcessVerticalSpace = true;
 * 		gridData.horizontalSpan = 2;
 * 		button2.setLayoutData(gridData);
 * </pre>
 * The second is to take advantage of <code>GridData</code> convenience constructors, for example:
 * <pre>
 *      button1.setLayoutData(new GridData (SWT.FILL, SWT.CENTER, true, false));
 *      button2.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, true, 2, 1));
 * </pre>
 * <p>
 * NOTE: Do not reuse <code>GridData</code> objects. Every control in a
 * <code>Composite</code> that is managed by a <code>GridLayout</code>
 * must have a unique <code>GridData</code> object. If the layout data
 * for a control in a <code>GridLayout</code> is null at layout time,
 * a unique <code>GridData</code> object is created for it.
 * </p>
 *
 * @see GridLayout
 * @see Control#setLayoutData
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class SwtGridData implements IGridData {

    int cacheWidth = -1, cacheHeight = -1;

    int defaultWhint, defaultHhint, defaultWidth = -1, defaultHeight = -1;

    int currentWhint, currentHhint, currentWidth = -1, currentHeight = -1;

    /**
     * Constructs a new instance of GridData using
     * default values.
     */
    public SwtGridData(GridData api) {
        super();
        setApi(api);
    }

    /**
     * Constructs a new instance based on the GridData style.
     * This constructor is not recommended.
     *
     * @param style the GridData style
     */
    public SwtGridData(int style, GridData api) {
        super();
        setApi(api);
        if ((style & GridData.VERTICAL_ALIGN_BEGINNING) != 0)
            getApi().verticalAlignment = GridData.BEGINNING;
        if ((style & GridData.VERTICAL_ALIGN_CENTER) != 0)
            getApi().verticalAlignment = GridData.CENTER;
        if ((style & GridData.VERTICAL_ALIGN_FILL) != 0)
            getApi().verticalAlignment = GridData.FILL;
        if ((style & GridData.VERTICAL_ALIGN_END) != 0)
            getApi().verticalAlignment = GridData.END;
        if ((style & GridData.HORIZONTAL_ALIGN_BEGINNING) != 0)
            getApi().horizontalAlignment = GridData.BEGINNING;
        if ((style & GridData.HORIZONTAL_ALIGN_CENTER) != 0)
            getApi().horizontalAlignment = GridData.CENTER;
        if ((style & GridData.HORIZONTAL_ALIGN_FILL) != 0)
            getApi().horizontalAlignment = GridData.FILL;
        if ((style & GridData.HORIZONTAL_ALIGN_END) != 0)
            getApi().horizontalAlignment = GridData.END;
        getApi().grabExcessHorizontalSpace = (style & GridData.GRAB_HORIZONTAL) != 0;
        getApi().grabExcessVerticalSpace = (style & GridData.GRAB_VERTICAL) != 0;
    }

    /**
     * Constructs a new instance of GridData according to the parameters.
     *
     * @param horizontalAlignment how control will be positioned horizontally within a cell,
     * 		one of: SWT.BEGINNING (or SWT.LEFT), SWT.CENTER, SWT.END (or SWT.RIGHT), or SWT.FILL
     * @param verticalAlignment how control will be positioned vertically within a cell,
     * 		one of: SWT.BEGINNING (or SWT.TOP), SWT.CENTER, SWT.END (or SWT.BOTTOM), or SWT.FILL
     * @param grabExcessHorizontalSpace whether cell will be made wide enough to fit the remaining horizontal space
     * @param grabExcessVerticalSpace whether cell will be made high enough to fit the remaining vertical space
     *
     * @since 3.0
     */
    public SwtGridData(int horizontalAlignment, int verticalAlignment, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace, GridData api) {
        this(horizontalAlignment, verticalAlignment, grabExcessHorizontalSpace, grabExcessVerticalSpace, 1, 1, api);
    }

    /**
     * Constructs a new instance of GridData according to the parameters.
     *
     * @param horizontalAlignment how control will be positioned horizontally within a cell,
     * 		one of: SWT.BEGINNING (or SWT.LEFT), SWT.CENTER, SWT.END (or SWT.RIGHT), or SWT.FILL
     * @param verticalAlignment how control will be positioned vertically within a cell,
     * 		one of: SWT.BEGINNING (or SWT.TOP), SWT.CENTER, SWT.END (or SWT.BOTTOM), or SWT.FILL
     * @param grabExcessHorizontalSpace whether cell will be made wide enough to fit the remaining horizontal space
     * @param grabExcessVerticalSpace whether cell will be made high enough to fit the remaining vertical space
     * @param horizontalSpan the number of column cells that the control will take up
     * @param verticalSpan the number of row cells that the control will take up
     *
     * @since 3.0
     */
    public SwtGridData(int horizontalAlignment, int verticalAlignment, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace, int horizontalSpan, int verticalSpan, GridData api) {
        super();
        setApi(api);
        this.getApi().horizontalAlignment = horizontalAlignment;
        this.getApi().verticalAlignment = verticalAlignment;
        this.getApi().grabExcessHorizontalSpace = grabExcessHorizontalSpace;
        this.getApi().grabExcessVerticalSpace = grabExcessVerticalSpace;
        this.getApi().horizontalSpan = horizontalSpan;
        this.getApi().verticalSpan = verticalSpan;
    }

    /**
     * Constructs a new instance of GridData according to the parameters.
     * A value of SWT.DEFAULT indicates that no minimum width or
     * no minimum height is specified.
     *
     * @param width a minimum width for the column
     * @param height a minimum height for the row
     *
     * @since 3.0
     */
    public SwtGridData(int width, int height, GridData api) {
        super();
        setApi(api);
        this.getApi().widthHint = width;
        this.getApi().heightHint = height;
    }

    void computeSize(Control control, int wHint, int hHint, boolean flushCache) {
        if (cacheWidth != -1 && cacheHeight != -1)
            return;
        if (wHint == this.getApi().widthHint && hHint == this.getApi().heightHint) {
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
        defaultWidth = defaultHeight = -1;
        currentWidth = currentHeight = -1;
    }

    String getName() {
        String string = getClass().getName();
        int index = string.lastIndexOf('.');
        if (index == -1)
            return string;
        return string.substring(index + 1, string.length());
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the GridData object
     */
    @Override
    public String toString() {
        String hAlign = "";
        hAlign = switch(getApi().horizontalAlignment) {
            case SWT.FILL ->
                "SWT.FILL";
            case SWT.BEGINNING ->
                "SWT.BEGINNING";
            case SWT.LEFT ->
                "SWT.LEFT";
            case SWT.END ->
                "SWT.END";
            case GridData.END ->
                "GridData.END";
            case SWT.RIGHT ->
                "SWT.RIGHT";
            case SWT.CENTER ->
                "SWT.CENTER";
            case GridData.CENTER ->
                "GridData.CENTER";
            default ->
                "Undefined " + getApi().horizontalAlignment;
        };
        String vAlign = "";
        vAlign = switch(getApi().verticalAlignment) {
            case SWT.FILL ->
                "SWT.FILL";
            case SWT.BEGINNING ->
                "SWT.BEGINNING";
            case SWT.TOP ->
                "SWT.TOP";
            case SWT.END ->
                "SWT.END";
            case GridData.END ->
                "GridData.END";
            case SWT.BOTTOM ->
                "SWT.BOTTOM";
            case SWT.CENTER ->
                "SWT.CENTER";
            case GridData.CENTER ->
                "GridData.CENTER";
            default ->
                "Undefined " + getApi().verticalAlignment;
        };
        String string = getName() + " {";
        string += "horizontalAlignment=" + hAlign + " ";
        if (getApi().horizontalIndent != 0)
            string += "horizontalIndent=" + getApi().horizontalIndent + " ";
        if (getApi().horizontalSpan != 1)
            string += "horizontalSpan=" + getApi().horizontalSpan + " ";
        if (getApi().grabExcessHorizontalSpace)
            string += "grabExcessHorizontalSpace=" + getApi().grabExcessHorizontalSpace + " ";
        if (getApi().widthHint != SWT.DEFAULT)
            string += "widthHint=" + getApi().widthHint + " ";
        if (getApi().minimumWidth != 0)
            string += "minimumWidth=" + getApi().minimumWidth + " ";
        string += "verticalAlignment=" + vAlign + " ";
        if (getApi().verticalIndent != 0)
            string += "verticalIndent=" + getApi().verticalIndent + " ";
        if (getApi().verticalSpan != 1)
            string += "verticalSpan=" + getApi().verticalSpan + " ";
        if (getApi().grabExcessVerticalSpace)
            string += "grabExcessVerticalSpace=" + getApi().grabExcessVerticalSpace + " ";
        if (getApi().heightHint != SWT.DEFAULT)
            string += "heightHint=" + getApi().heightHint + " ";
        if (getApi().minimumHeight != 0)
            string += "minimumHeight=" + getApi().minimumHeight + " ";
        if (getApi().exclude)
            string += "exclude=" + getApi().exclude + " ";
        string = string.trim();
        string += "}";
        return string;
    }

    public GridData getApi() {
        if (api == null)
            api = GridData.createApi(this);
        return (GridData) api;
    }

    protected GridData api;

    public void setApi(GridData api) {
        this.api = api;
        if (api != null)
            api.impl = this;
    }
}
