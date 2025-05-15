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
 * Instances of this class determine the size and position of the
 * children of a <code>Composite</code> by placing them either in
 * horizontal rows or vertical columns within the parent <code>Composite</code>.
 * <p>
 * <code>RowLayout</code> aligns all controls in one row if the
 * <code>type</code> is set to horizontal, and one column if it is
 * set to vertical. It has the ability to wrap, and provides configurable
 * margins and spacing. <code>RowLayout</code> has a number of configuration
 * fields. In addition, the height and width of each control in a
 * <code>RowLayout</code> can be specified by setting a <code>RowData</code>
 * object into the control using <code>setLayoutData ()</code>.
 * </p>
 * <p>
 * The following example code creates a <code>RowLayout</code>, sets all
 * of its fields to non-default values, and then sets it into a
 * <code>Shell</code>.</p>
 * <pre>
 * 		RowLayout rowLayout = new RowLayout();
 * 		rowLayout.wrap = false;
 * 		rowLayout.pack = false;
 * 		rowLayout.justify = true;
 * 		rowLayout.type = SWT.VERTICAL;
 * 		rowLayout.marginLeft = 5;
 * 		rowLayout.marginTop = 5;
 * 		rowLayout.marginRight = 5;
 * 		rowLayout.marginBottom = 5;
 * 		rowLayout.spacing = 0;
 * 		shell.setLayout(rowLayout);
 * </pre>
 * If you are using the default field values, you only need one line of code:
 * <pre>
 * 		shell.setLayout(new RowLayout());
 * </pre>
 *
 * @see RowData
 * @see <a href="http://www.eclipse.org/swt/snippets/#rowlayout">RowLayout snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: LayoutExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class RowLayout extends Layout {

    /**
     * type specifies whether the layout places controls in rows or
     * columns.
     *
     * The default value is HORIZONTAL.
     *
     * Possible values are: <ul>
     *    <li>HORIZONTAL: Position the controls horizontally from left to right</li>
     *    <li>VERTICAL: Position the controls vertically from top to bottom</li>
     * </ul>
     *
     * @since 2.0
     */
    public int type = SWT.HORIZONTAL;

    /**
     * marginWidth specifies the number of points of horizontal margin
     * that will be placed along the left and right edges of the layout.
     *
     * The default value is 0.
     *
     * @since 3.0
     */
    public int marginWidth = 0;

    /**
     * marginHeight specifies the number of points of vertical margin
     * that will be placed along the top and bottom edges of the layout.
     *
     * The default value is 0.
     *
     * @since 3.0
     */
    public int marginHeight = 0;

    /**
     * spacing specifies the number of points between the edge of one cell
     * and the edge of its neighbouring cell.
     *
     * The default value is 3.
     */
    public int spacing = 3;

    /**
     * wrap specifies whether a control will be wrapped to the next
     * row if there is insufficient space on the current row.
     *
     * The default value is true.
     */
    public boolean wrap = true;

    /**
     * pack specifies whether all controls in the layout take
     * their preferred size.  If pack is false, all controls will
     * have the same size which is the size required to accommodate the
     * largest preferred height and the largest preferred width of all
     * the controls in the layout.
     *
     * The default value is true.
     */
    public boolean pack = true;

    /**
     * fill specifies whether the controls in a row should be
     * all the same height for horizontal layouts, or the same
     * width for vertical layouts.
     *
     * The default value is false.
     *
     * @since 3.0
     */
    public boolean fill = false;

    /**
     * center specifies whether the controls in a row should be
     * centered vertically in each cell for horizontal layouts,
     * or centered horizontally in each cell for vertical layouts.
     *
     * The default value is false.
     *
     * @since 3.4
     */
    public boolean center = false;

    /**
     * justify specifies whether the controls in a row should be
     * fully justified, with any extra space placed between the controls.
     *
     * The default value is false.
     */
    public boolean justify = false;

    /**
     * marginLeft specifies the number of points of horizontal margin
     * that will be placed along the left edge of the layout.
     *
     * The default value is 3.
     */
    public int marginLeft = 3;

    /**
     * marginTop specifies the number of points of vertical margin
     * that will be placed along the top edge of the layout.
     *
     * The default value is 3.
     */
    public int marginTop = 3;

    /**
     * marginRight specifies the number of points of horizontal margin
     * that will be placed along the right edge of the layout.
     *
     * The default value is 3.
     */
    public int marginRight = 3;

    /**
     * marginBottom specifies the number of points of vertical margin
     * that will be placed along the bottom edge of the layout.
     *
     * The default value is 3.
     */
    public int marginBottom = 3;

    /**
     * Constructs a new instance of this class with type HORIZONTAL.
     */
    public RowLayout() {
        this(new nat.org.eclipse.swt.layout.RowLayout());
    }

    /**
     * Constructs a new instance of this class given the type.
     *
     * @param type the type of row layout
     *
     * @since 2.0
     */
    public RowLayout(int type) {
        this(new nat.org.eclipse.swt.layout.RowLayout(type));
    }

    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        IPoint ret = getDelegate().computeSize((composite != null ? composite.getDelegate() : null), wHint, hHint, flushCache);
        return ret != null ? ret.getApi() : null;
    }

    protected boolean flushCache(Control control) {
        return getDelegate().flushCache((control != null ? control.getDelegate() : null));
    }

    protected void layout(Composite composite, boolean flushCache) {
        getDelegate().layout((composite != null ? composite.getDelegate() : null), flushCache);
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

    protected RowLayout(IRowLayout delegate) {
        super(delegate);
    }

    public static RowLayout createApi(IRowLayout delegate) {
        return new RowLayout(delegate);
    }

    public IRowLayout getDelegate() {
        return (IRowLayout) super.getDelegate();
    }
}
