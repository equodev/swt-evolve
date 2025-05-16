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
public final class RowData {

    /**
     * width specifies the desired width in points. This value
     * is the wHint passed into Control.computeSize(int, int, boolean)
     * to determine the preferred size of the control.
     *
     * The default value is SWT.DEFAULT.
     *
     * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
     */
    public int width = SWT.DEFAULT;

    /**
     * height specifies the preferred height in points. This value
     * is the hHint passed into Control.computeSize(int, int, boolean)
     * to determine the preferred size of the control.
     *
     * The default value is SWT.DEFAULT.
     *
     * @see org.eclipse.swt.widgets.Control#computeSize(int, int, boolean)
     */
    public int height = SWT.DEFAULT;

    /**
     * exclude informs the layout to ignore this control when sizing
     * and positioning controls.  If this value is <code>true</code>,
     * the size and position of the control will not be managed by the
     * layout.  If this	value is <code>false</code>, the size and
     * position of the control will be computed and assigned.
     *
     * The default value is <code>false</code>.
     *
     * @since 3.1
     */
    public boolean exclude = false;

    /**
     * Constructs a new instance of RowData using
     * default values.
     */
    public RowData() {
        this(new SwtRowData());
    }

    /**
     * Constructs a new instance of RowData according to the parameters.
     * A value of SWT.DEFAULT indicates that no minimum width or
     * no minimum height is specified.
     *
     * @param width a minimum width for the control
     * @param height a minimum height for the control
     */
    public RowData(int width, int height) {
        this(new SwtRowData(width, height));
    }

    /**
     * Constructs a new instance of RowData according to the parameter.
     * A value of SWT.DEFAULT indicates that no minimum width or
     * no minimum height is specified.
     *
     * @param point a point whose x coordinate specifies a minimum width for the control
     * and y coordinate specifies a minimum height for the control
     */
    public RowData(Point point) {
        this(new SwtRowData(point));
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the RowData object
     */
    public String toString() {
        return getImpl().toString();
    }

    IRowData impl;

    protected RowData(IRowData impl) {
        this.impl = impl;
        impl.setApi(this);
    }

    public static RowData createApi(IRowData impl) {
        return new RowData(impl);
    }

    public IRowData getImpl() {
        return impl;
    }
}
