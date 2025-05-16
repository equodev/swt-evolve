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

/**
 * Each control controlled by a <code>RowLayout</code> can have its initial
 * width and height specified by setting a <code>RowData</code> object
 * into the control.
 * <p>
 * The following code uses a <code>RowData</code> object to change the initial
 * size of a <code>Button</code> in a <code>Shell</code>:
 * </p>
 * <pre>
 * 		Display display = new Display();
 * 		Shell shell = new Shell(display);
 * 		shell.setLayout(new RowLayout());
 * 		Button button1 = new Button(shell, SWT.PUSH);
 * 		button1.setText("Button 1");
 * 		button1.setLayoutData(new RowData(50, 40));
 * </pre>
 *
 * @see RowLayout
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class SwtRowData implements IRowData {

    /**
     * Constructs a new instance of RowData using
     * default values.
     */
    public SwtRowData() {
    }

    /**
     * Constructs a new instance of RowData according to the parameters.
     * A value of SWT.DEFAULT indicates that no minimum width or
     * no minimum height is specified.
     *
     * @param width a minimum width for the control
     * @param height a minimum height for the control
     */
    public SwtRowData(int width, int height) {
        this.getApi().width = width;
        this.getApi().height = height;
    }

    /**
     * Constructs a new instance of RowData according to the parameter.
     * A value of SWT.DEFAULT indicates that no minimum width or
     * no minimum height is specified.
     *
     * @param point a point whose x coordinate specifies a minimum width for the control
     * and y coordinate specifies a minimum height for the control
     */
    public SwtRowData(Point point) {
        this(point.x, point.y);
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
     * @return a string representation of the RowData object
     */
    @Override
    public String toString() {
        String string = getName() + " {";
        if (getApi().width != SWT.DEFAULT)
            string += "width=" + getApi().width + " ";
        if (getApi().height != SWT.DEFAULT)
            string += "height=" + getApi().height + " ";
        if (getApi().exclude)
            string += "exclude=" + getApi().exclude + " ";
        string = string.trim();
        string += "}";
        return string;
    }

    public RowData getApi() {
        if (api == null)
            api = RowData.createApi(this);
        return (RowData) api;
    }

    protected RowData api;

    public void setApi(RowData api) {
        this.api = api;
    }
}
