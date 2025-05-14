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
 * Instances of this class lay out the control children of a
 * <code>Composite</code> in a grid.
 * <p>
 * <code>GridLayout</code> has a number of configuration fields, and the
 * controls it lays out can have an associated layout data object, called
 * <code>GridData</code>. The power of <code>GridLayout</code> lies in the
 * ability to configure <code>GridData</code> for each control in the layout.
 * </p>
 * <p>
 * The following code creates a shell managed by a <code>GridLayout</code>
 * with 3 columns:</p>
 * <pre>
 * 		Display display = new Display();
 * 		Shell shell = new Shell(display);
 * 		GridLayout gridLayout = new GridLayout();
 * 		gridLayout.numColumns = 3;
 * 		shell.setLayout(gridLayout);
 * </pre>
 * <p>
 * The <code>numColumns</code> field is the most important field in a
 * <code>GridLayout</code>. Widgets are laid out in columns from left
 * to right, and a new row is created when <code>numColumns</code> + 1
 * controls are added to the <code>Composite</code>.
 * </p>
 *
 * @see GridData
 * @see <a href="http://www.eclipse.org/swt/snippets/#gridlayout">GridLayout snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: LayoutExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class GridLayout extends Layout {

    /**
     * numColumns specifies the number of cell columns in the layout.
     * If numColumns has a value less than 1, the layout will not
     * set the size and position of any controls.
     *
     * The default value is 1.
     */
    public int numColumns = 1;

    /**
     * makeColumnsEqualWidth specifies whether all columns in the layout
     * will be forced to have the same width.
     *
     * The default value is false.
     */
    public boolean makeColumnsEqualWidth = false;

    /**
     * marginWidth specifies the number of points of horizontal margin
     * that will be placed along the left and right edges of the layout.
     *
     * The default value is 5.
     */
    public int marginWidth = 5;

    /**
     * marginHeight specifies the number of points of vertical margin
     * that will be placed along the top and bottom edges of the layout.
     *
     * The default value is 5.
     */
    public int marginHeight = 5;

    /**
     * marginLeft specifies the number of points of horizontal margin
     * that will be placed along the left edge of the layout.
     *
     * The default value is 0.
     *
     * @since 3.1
     */
    public int marginLeft = 0;

    /**
     * marginTop specifies the number of points of vertical margin
     * that will be placed along the top edge of the layout.
     *
     * The default value is 0.
     *
     * @since 3.1
     */
    public int marginTop = 0;

    /**
     * marginRight specifies the number of points of horizontal margin
     * that will be placed along the right edge of the layout.
     *
     * The default value is 0.
     *
     * @since 3.1
     */
    public int marginRight = 0;

    /**
     * marginBottom specifies the number of points of vertical margin
     * that will be placed along the bottom edge of the layout.
     *
     * The default value is 0.
     *
     * @since 3.1
     */
    public int marginBottom = 0;

    /**
     * horizontalSpacing specifies the number of points between the right
     * edge of one cell and the left edge of its neighbouring cell to
     * the right.
     *
     * The default value is 5.
     */
    public int horizontalSpacing = 5;

    /**
     * verticalSpacing specifies the number of points between the bottom
     * edge of one cell and the top edge of its neighbouring cell underneath.
     *
     * The default value is 5.
     */
    public int verticalSpacing = 5;

    /**
     * Constructs a new instance of this class
     * with a single column.
     */
    public GridLayout() {
        this(new nat.org.eclipse.swt.layout.GridLayout());
    }

    /**
     * Constructs a new instance of this class given the
     * number of columns, and whether or not the columns
     * should be forced to have the same width.
     * If numColumns has a value less than 1, the layout will not
     * set the size and position of any controls.
     *
     * @param numColumns the number of columns in the grid
     * @param makeColumnsEqualWidth whether or not the columns will have equal width
     *
     * @since 2.0
     */
    public GridLayout(int numColumns, boolean makeColumnsEqualWidth) {
        this(new nat.org.eclipse.swt.layout.GridLayout(numColumns, makeColumnsEqualWidth));
    }

    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        return getDelegate().computeSize(composite.getDelegate(), wHint, hHint, flushCache).getApi();
    }

    protected boolean flushCache(Control control) {
        return getDelegate().flushCache(control.getDelegate());
    }

    protected void layout(Composite composite, boolean flushCache) {
        getDelegate().layout(composite.getDelegate(), flushCache);
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the layout
     */
    public String toString() {
        return getDelegate().toString();
    }

    protected GridLayout(IGridLayout delegate) {
        super(delegate);
    }

    public IGridLayout getDelegate() {
        return (IGridLayout) super.getDelegate();
    }
}
