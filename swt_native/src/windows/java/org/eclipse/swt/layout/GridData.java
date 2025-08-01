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
public final class GridData {

    /**
     * verticalAlignment specifies how controls will be positioned
     * vertically within a cell.
     *
     * The default value is CENTER.
     *
     * Possible values are: <ul>
     *    <li>SWT.BEGINNING (or SWT.TOP): Position the control at the top of the cell</li>
     *    <li>SWT.CENTER: Position the control in the vertical center of the cell</li>
     *    <li>SWT.END (or SWT.BOTTOM): Position the control at the bottom of the cell</li>
     *    <li>SWT.FILL: Resize the control to fill the cell vertically</li>
     * </ul>
     */
    public int verticalAlignment = CENTER;

    /**
     * horizontalAlignment specifies how controls will be positioned
     * horizontally within a cell.
     *
     * The default value is BEGINNING.
     *
     * Possible values are: <ul>
     *    <li>SWT.BEGINNING (or SWT.LEFT): Position the control at the left of the cell</li>
     *    <li>SWT.CENTER: Position the control in the horizontal center of the cell</li>
     *    <li>SWT.END (or SWT.RIGHT): Position the control at the right of the cell</li>
     *    <li>SWT.FILL: Resize the control to fill the cell horizontally</li>
     * </ul>
     */
    public int horizontalAlignment = BEGINNING;

    /**
     * widthHint specifies the preferred width in points. This value
     * is the wHint passed into Control.computeSize(int, int, boolean)
     * to determine the preferred size of the control.
     *
     * The default value is SWT.DEFAULT.
     *
     * @see Control#computeSize(int, int, boolean)
     */
    public int widthHint = SWT.DEFAULT;

    /**
     * heightHint specifies the preferred height in points. This value
     * is the hHint passed into Control.computeSize(int, int, boolean)
     * to determine the preferred size of the control.
     *
     * The default value is SWT.DEFAULT.
     *
     * @see Control#computeSize(int, int, boolean)
     */
    public int heightHint = SWT.DEFAULT;

    /**
     * horizontalIndent specifies the number of points of indentation
     * that will be placed along the left side of the cell.
     *
     * The default value is 0.
     */
    public int horizontalIndent = 0;

    /**
     * verticalIndent specifies the number of points of indentation
     * that will be placed along the top side of the cell.
     *
     * The default value is 0.
     *
     * @since 3.1
     */
    public int verticalIndent = 0;

    /**
     * horizontalSpan specifies the number of column cells that the control
     * will take up.
     *
     * The default value is 1.
     */
    public int horizontalSpan = 1;

    /**
     * verticalSpan specifies the number of row cells that the control
     * will take up.
     *
     * The default value is 1.
     */
    public int verticalSpan = 1;

    /**
     * <p>grabExcessHorizontalSpace specifies whether the width of the cell
     * changes depending on the size of the parent Composite.  If
     * grabExcessHorizontalSpace is <code>true</code>, the following rules
     * apply to the width of the cell:</p>
     * <ul>
     * <li>If extra horizontal space is available in the parent, the cell will
     * grow to be wider than its preferred width.  The new width
     * will be "preferred width + delta" where delta is the extra
     * horizontal space divided by the number of grabbing columns.</li>
     * <li>If there is not enough horizontal space available in the parent, the
     * cell will shrink until it reaches its minimum width as specified by
     * GridData.minimumWidth. The new width will be the maximum of
     * "minimumWidth" and "preferred width - delta", where delta is
     * the amount of space missing divided by the number of grabbing columns.</li>
     * <li>If the parent is packed, the cell will be its preferred width
     * as specified by GridData.widthHint.</li>
     * <li>If the control spans multiple columns and there are no other grabbing
     * controls in any of the spanned columns, the last column in the span will
     * grab the extra space.  If there is at least one other grabbing control
     * in the span, the grabbing will be spread over the columns already
     * marked as grabExcessHorizontalSpace.</li>
     * </ul>
     *
     * <p>The default value is false.</p>
     *
     * @see GridData#minimumWidth
     * @see GridData#widthHint
     */
    public boolean grabExcessHorizontalSpace = false;

    /**
     * <p>grabExcessVerticalSpace specifies whether the height of the cell
     * changes depending on the size of the parent Composite.  If
     * grabExcessVerticalSpace is <code>true</code>, the following rules
     * apply to the height of the cell:</p>
     * <ul>
     * <li>If extra vertical space is available in the parent, the cell will
     * grow to be taller than its preferred height.  The new height
     * will be "preferred height + delta" where delta is the extra
     * vertical space divided by the number of grabbing rows.</li>
     * <li>If there is not enough vertical space available in the parent, the
     * cell will shrink until it reaches its minimum height as specified by
     * GridData.minimumHeight. The new height will be the maximum of
     * "minimumHeight" and "preferred height - delta", where delta is
     * the amount of space missing divided by the number of grabbing rows.</li>
     * <li>If the parent is packed, the cell will be its preferred height
     * as specified by GridData.heightHint.</li>
     * <li>If the control spans multiple rows and there are no other grabbing
     * controls in any of the spanned rows, the last row in the span will
     * grab the extra space.  If there is at least one other grabbing control
     * in the span, the grabbing will be spread over the rows already
     * marked as grabExcessVerticalSpace.</li>
     * </ul>
     *
     * <p>The default value is false.</p>
     *
     * @see GridData#minimumHeight
     * @see GridData#heightHint
     */
    public boolean grabExcessVerticalSpace = false;

    /**
     * minimumWidth specifies the minimum width in points.  This value
     * applies only if grabExcessHorizontalSpace is true. A value of
     * SWT.DEFAULT means that the minimum width will be the result
     * of Control.computeSize(int, int, boolean) where wHint is
     * determined by GridData.widthHint.
     *
     * The default value is 0.
     *
     * @since 3.1
     * @see Control#computeSize(int, int, boolean)
     * @see GridData#widthHint
     */
    public int minimumWidth = 0;

    /**
     * minimumHeight specifies the minimum height in points.  This value
     * applies only if grabExcessVerticalSpace is true.  A value of
     * SWT.DEFAULT means that the minimum height will be the result
     * of Control.computeSize(int, int, boolean) where hHint is
     * determined by GridData.heightHint.
     *
     * The default value is 0.
     *
     * @since 3.1
     * @see Control#computeSize(int, int, boolean)
     * @see GridData#heightHint
     */
    public int minimumHeight = 0;

    /**
     * exclude informs the layout to ignore this control when sizing
     * and positioning controls.  If this value is <code>true</code>,
     * the size and position of the control will not be managed by the
     * layout.  If this value is <code>false</code>, the size and
     * position of the control will be computed and assigned.
     *
     * The default value is <code>false</code>.
     *
     * @since 3.1
     */
    public boolean exclude = false;

    /**
     * Value for horizontalAlignment or verticalAlignment.
     * Position the control at the top or left of the cell.
     * Not recommended. Use SWT.BEGINNING, SWT.TOP or SWT.LEFT instead.
     */
    public static final int BEGINNING = SWT.BEGINNING;

    /**
     * Value for horizontalAlignment or verticalAlignment.
     * Position the control in the vertical or horizontal center of the cell
     * Not recommended. Use SWT.CENTER instead.
     */
    public static final int CENTER = 2;

    /**
     * Value for horizontalAlignment or verticalAlignment.
     * Position the control at the bottom or right of the cell
     * Not recommended. Use SWT.END, SWT.BOTTOM or SWT.RIGHT instead.
     */
    public static final int END = 3;

    /**
     * Value for horizontalAlignment or verticalAlignment.
     * Resize the control to fill the cell horizontally or vertically.
     * Not recommended. Use SWT.FILL instead.
     */
    public static final int FILL = SWT.FILL;

    /**
     * Style bit for <code>new GridData(int)</code>.
     * Position the control at the top of the cell.
     * Not recommended. Use
     * <code>new GridData(int, SWT.BEGINNING, boolean, boolean)</code>
     * instead.
     */
    public static final int VERTICAL_ALIGN_BEGINNING = 1 << 1;

    /**
     * Style bit for <code>new GridData(int)</code> to position the
     * control in the vertical center of the cell.
     * Not recommended. Use
     * <code>new GridData(int, SWT.CENTER, boolean, boolean)</code>
     * instead.
     */
    public static final int VERTICAL_ALIGN_CENTER = 1 << 2;

    /**
     * Style bit for <code>new GridData(int)</code> to position the
     * control at the bottom of the cell.
     * Not recommended. Use
     * <code>new GridData(int, SWT.END, boolean, boolean)</code>
     * instead.
     */
    public static final int VERTICAL_ALIGN_END = 1 << 3;

    /**
     * Style bit for <code>new GridData(int)</code> to resize the
     * control to fill the cell vertically.
     * Not recommended. Use
     * <code>new GridData(int, SWT.FILL, boolean, boolean)</code>
     * instead
     */
    public static final int VERTICAL_ALIGN_FILL = 1 << 4;

    /**
     * Style bit for <code>new GridData(int)</code> to position the
     * control at the left of the cell.
     * Not recommended. Use
     * <code>new GridData(SWT.BEGINNING, int, boolean, boolean)</code>
     * instead.
     */
    public static final int HORIZONTAL_ALIGN_BEGINNING = 1 << 5;

    /**
     * Style bit for <code>new GridData(int)</code> to position the
     * control in the horizontal center of the cell.
     * Not recommended. Use
     * <code>new GridData(SWT.CENTER, int, boolean, boolean)</code>
     * instead.
     */
    public static final int HORIZONTAL_ALIGN_CENTER = 1 << 6;

    /**
     * Style bit for <code>new GridData(int)</code> to position the
     * control at the right of the cell.
     * Not recommended. Use
     * <code>new GridData(SWT.END, int, boolean, boolean)</code>
     * instead.
     */
    public static final int HORIZONTAL_ALIGN_END = 1 << 7;

    /**
     * Style bit for <code>new GridData(int)</code> to resize the
     * control to fill the cell horizontally.
     * Not recommended. Use
     * <code>new GridData(SWT.FILL, int, boolean, boolean)</code>
     * instead.
     */
    public static final int HORIZONTAL_ALIGN_FILL = 1 << 8;

    /**
     * Style bit for <code>new GridData(int)</code> to resize the
     * control to fit the remaining horizontal space.
     * Not recommended. Use
     * <code>new GridData(int, int, true, boolean)</code>
     * instead.
     */
    public static final int GRAB_HORIZONTAL = 1 << 9;

    /**
     * Style bit for <code>new GridData(int)</code> to resize the
     * control to fit the remaining vertical space.
     * Not recommended. Use
     * <code>new GridData(int, int, boolean, true)</code>
     * instead.
     */
    public static final int GRAB_VERTICAL = 1 << 10;

    /**
     * Style bit for <code>new GridData(int)</code> to resize the
     * control to fill the cell vertically and to fit the remaining
     * vertical space.
     * FILL_VERTICAL = VERTICAL_ALIGN_FILL | GRAB_VERTICAL
     * Not recommended. Use
     * <code>new GridData(int, SWT.FILL, boolean, true)</code>
     * instead.
     */
    public static final int FILL_VERTICAL = VERTICAL_ALIGN_FILL | GRAB_VERTICAL;

    /**
     * Style bit for <code>new GridData(int)</code> to resize the
     * control to fill the cell horizontally and to fit the remaining
     * horizontal space.
     * FILL_HORIZONTAL = HORIZONTAL_ALIGN_FILL | GRAB_HORIZONTAL
     * Not recommended. Use
     * <code>new GridData(SWT.FILL, int, true, boolean)</code>
     * instead.
     */
    public static final int FILL_HORIZONTAL = HORIZONTAL_ALIGN_FILL | GRAB_HORIZONTAL;

    /**
     * Style bit for <code>new GridData(int)</code> to resize the
     * control to fill the cell horizontally and vertically and
     * to fit the remaining horizontal and vertical space.
     * FILL_BOTH = FILL_VERTICAL | FILL_HORIZONTAL
     * Not recommended. Use
     * <code>new GridData(SWT.FILL, SWT.FILL, true, true)</code>
     * instead.
     */
    public static final int FILL_BOTH = FILL_VERTICAL | FILL_HORIZONTAL;

    /**
     * Constructs a new instance of GridData using
     * default values.
     */
    public GridData() {
        this((IGridData) null);
        setImpl(new SwtGridData(this));
    }

    /**
     * Constructs a new instance based on the GridData style.
     * This constructor is not recommended.
     *
     * @param style the GridData style
     */
    public GridData(int style) {
        this((IGridData) null);
        setImpl(new SwtGridData(style, this));
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
    public GridData(int horizontalAlignment, int verticalAlignment, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace) {
        this((IGridData) null);
        setImpl(new SwtGridData(horizontalAlignment, verticalAlignment, grabExcessHorizontalSpace, grabExcessVerticalSpace, this));
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
    public GridData(int horizontalAlignment, int verticalAlignment, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace, int horizontalSpan, int verticalSpan) {
        this((IGridData) null);
        setImpl(new SwtGridData(horizontalAlignment, verticalAlignment, grabExcessHorizontalSpace, grabExcessVerticalSpace, horizontalSpan, verticalSpan, this));
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
    public GridData(int width, int height) {
        this((IGridData) null);
        setImpl(new SwtGridData(width, height, this));
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the GridData object
     */
    public String toString() {
        return getImpl().toString();
    }

    protected IGridData impl;

    protected GridData(IGridData impl) {
        if (impl != null)
            impl.setApi(this);
    }

    static GridData createApi(IGridData impl) {
        return new GridData(impl);
    }

    public IGridData getImpl() {
        return impl;
    }

    protected GridData setImpl(IGridData impl) {
        this.impl = impl;
        return this;
    }
}
