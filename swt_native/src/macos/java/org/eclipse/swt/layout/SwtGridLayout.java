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
public final class SwtGridLayout extends SwtLayout implements IGridLayout {

    /**
     * Constructs a new instance of this class
     * with a single column.
     */
    public SwtGridLayout(GridLayout api) {
        super(api);
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
    public SwtGridLayout(int numColumns, boolean makeColumnsEqualWidth, GridLayout api) {
        super(api);
        this.getApi().numColumns = numColumns;
        this.getApi().makeColumnsEqualWidth = makeColumnsEqualWidth;
    }

    @Override
    public Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
        Point size = layout(composite, false, 0, 0, wHint, hHint, flushCache);
        if (wHint != SWT.DEFAULT)
            size.x = wHint;
        if (hHint != SWT.DEFAULT)
            size.y = hHint;
        return size;
    }

    @Override
    public boolean flushCache(Control control) {
        Object data = control.getLayoutData();
        if (data != null)
            ((SwtGridData) ((GridData) data).getImpl()).flushCache();
        return true;
    }

    GridData getData(Control[][] grid, int row, int column, int rowCount, int columnCount, boolean first) {
        Control control = grid[row][column];
        if (control != null) {
            GridData data = (GridData) control.getLayoutData();
            int hSpan = Math.max(1, Math.min(data.horizontalSpan, columnCount));
            int vSpan = Math.max(1, data.verticalSpan);
            int i = first ? row + vSpan - 1 : row - vSpan + 1;
            int j = first ? column + hSpan - 1 : column - hSpan + 1;
            if (0 <= i && i < rowCount) {
                if (0 <= j && j < columnCount) {
                    if (control == grid[i][j])
                        return data;
                }
            }
        }
        return null;
    }

    @Override
    public void layout(Composite composite, boolean flushCache) {
        Rectangle rect = composite.getClientArea();
        layout(composite, true, rect.x, rect.y, rect.width, rect.height, flushCache);
    }

    Point layout(Composite composite, boolean move, int x, int y, int width, int height, boolean flushCache) {
        if (getApi().numColumns < 1) {
            return new Point(getApi().marginLeft + getApi().marginWidth * 2 + getApi().marginRight, getApi().marginTop + getApi().marginHeight * 2 + getApi().marginBottom);
        }
        Control[] children = composite.getChildren();
        int count = 0;
        for (int i = 0; i < children.length; i++) {
            Control control = children[i];
            GridData data = (GridData) control.getLayoutData();
            if (data == null || !data.exclude) {
                children[count++] = children[i];
            }
        }
        if (count == 0) {
            return new Point(getApi().marginLeft + getApi().marginWidth * 2 + getApi().marginRight, getApi().marginTop + getApi().marginHeight * 2 + getApi().marginBottom);
        }
        for (int i = 0; i < count; i++) {
            Control child = children[i];
            GridData data = (GridData) child.getLayoutData();
            if (data == null)
                child.setLayoutData(data = new GridData());
            if (flushCache)
                ((SwtGridData) data.getImpl()).flushCache();
            ((SwtGridData) data.getImpl()).computeSize(child, data.widthHint, data.heightHint, flushCache);
            if (data.grabExcessHorizontalSpace && data.minimumWidth > 0) {
                if (((SwtGridData) data.getImpl()).cacheWidth < data.minimumWidth) {
                    int trim = 0;
                    //TEMPORARY CODE
                    if (child instanceof Scrollable) {
                        Rectangle rect = ((Scrollable) child).computeTrim(0, 0, 0, 0);
                        trim = rect.width;
                    } else {
                        trim = child.getBorderWidth() * 2;
                    }
                    ((SwtGridData) data.getImpl()).cacheWidth = ((SwtGridData) data.getImpl()).cacheHeight = SWT.DEFAULT;
                    ((SwtGridData) data.getImpl()).computeSize(child, Math.max(0, data.minimumWidth - trim), data.heightHint, false);
                }
            }
            if (data.grabExcessVerticalSpace && data.minimumHeight > 0) {
                ((SwtGridData) data.getImpl()).cacheHeight = Math.max(((SwtGridData) data.getImpl()).cacheHeight, data.minimumHeight);
            }
        }
        /* Build the grid */
        int row = 0, column = 0, rowCount = 0, columnCount = getApi().numColumns;
        Control[][] grid = new Control[4][columnCount];
        for (int i = 0; i < count; i++) {
            Control child = children[i];
            GridData data = (GridData) child.getLayoutData();
            int hSpan = Math.max(1, Math.min(data.horizontalSpan, columnCount));
            int vSpan = Math.max(1, data.verticalSpan);
            while (true) {
                int lastRow = row + vSpan;
                if (lastRow >= grid.length) {
                    Control[][] newGrid = new Control[lastRow + 4][columnCount];
                    System.arraycopy(grid, 0, newGrid, 0, grid.length);
                    grid = newGrid;
                }
                if (grid[row] == null) {
                    grid[row] = new Control[columnCount];
                }
                while (column < columnCount && grid[row][column] != null) {
                    column++;
                }
                int endCount = column + hSpan;
                if (endCount <= columnCount) {
                    int index = column;
                    while (index < endCount && grid[row][index] == null) {
                        index++;
                    }
                    if (index == endCount)
                        break;
                    column = index;
                }
                if (column + hSpan >= columnCount) {
                    column = 0;
                    row++;
                }
            }
            for (int j = 0; j < vSpan; j++) {
                if (grid[row + j] == null) {
                    grid[row + j] = new Control[columnCount];
                }
                for (int k = 0; k < hSpan; k++) {
                    grid[row + j][column + k] = child;
                }
            }
            rowCount = Math.max(rowCount, row + vSpan);
            column += hSpan;
        }
        /* Column widths */
        int availableWidth = width - getApi().horizontalSpacing * (columnCount - 1) - (getApi().marginLeft + getApi().marginWidth * 2 + getApi().marginRight);
        int expandCount = 0;
        int[] widths = new int[columnCount];
        int[] minWidths = new int[columnCount];
        boolean[] expandColumn = new boolean[columnCount];
        for (int j = 0; j < columnCount; j++) {
            for (int i = 0; i < rowCount; i++) {
                GridData data = getData(grid, i, j, rowCount, columnCount, true);
                if (data != null) {
                    int hSpan = Math.max(1, Math.min(data.horizontalSpan, columnCount));
                    if (hSpan == 1) {
                        int w = ((SwtGridData) data.getImpl()).cacheWidth + data.horizontalIndent;
                        widths[j] = Math.max(widths[j], w);
                        if (data.grabExcessHorizontalSpace) {
                            if (!expandColumn[j])
                                expandCount++;
                            expandColumn[j] = true;
                        }
                        if (!data.grabExcessHorizontalSpace || data.minimumWidth != 0) {
                            w = !data.grabExcessHorizontalSpace || data.minimumWidth == SWT.DEFAULT ? ((SwtGridData) data.getImpl()).cacheWidth : data.minimumWidth;
                            w += data.horizontalIndent;
                            minWidths[j] = Math.max(minWidths[j], w);
                        }
                    }
                }
            }
            for (int i = 0; i < rowCount; i++) {
                GridData data = getData(grid, i, j, rowCount, columnCount, false);
                if (data != null) {
                    int hSpan = Math.max(1, Math.min(data.horizontalSpan, columnCount));
                    if (hSpan > 1) {
                        int spanWidth = 0, spanMinWidth = 0, spanExpandCount = 0;
                        for (int k = 0; k < hSpan; k++) {
                            spanWidth += widths[j - k];
                            spanMinWidth += minWidths[j - k];
                            if (expandColumn[j - k])
                                spanExpandCount++;
                        }
                        if (data.grabExcessHorizontalSpace && spanExpandCount == 0) {
                            expandCount++;
                            expandColumn[j] = true;
                        }
                        int w = ((SwtGridData) data.getImpl()).cacheWidth + data.horizontalIndent - spanWidth - (hSpan - 1) * getApi().horizontalSpacing;
                        if (w > 0) {
                            if (getApi().makeColumnsEqualWidth) {
                                int equalWidth = (w + spanWidth) / hSpan;
                                int remainder = (w + spanWidth) % hSpan, last = -1;
                                for (int k = 0; k < hSpan; k++) {
                                    widths[last = j - k] = Math.max(equalWidth, widths[j - k]);
                                }
                                if (last > -1)
                                    widths[last] += remainder;
                            } else {
                                if (spanExpandCount == 0) {
                                    widths[j] += w;
                                } else {
                                    int delta = w / spanExpandCount;
                                    int remainder = w % spanExpandCount, last = -1;
                                    for (int k = 0; k < hSpan; k++) {
                                        if (expandColumn[j - k]) {
                                            widths[last = j - k] += delta;
                                        }
                                    }
                                    if (last > -1)
                                        widths[last] += remainder;
                                }
                            }
                        }
                        if (!data.grabExcessHorizontalSpace || data.minimumWidth != 0) {
                            w = !data.grabExcessHorizontalSpace || data.minimumWidth == SWT.DEFAULT ? ((SwtGridData) data.getImpl()).cacheWidth : data.minimumWidth;
                            w += data.horizontalIndent - spanMinWidth - (hSpan - 1) * getApi().horizontalSpacing;
                            if (w > 0) {
                                if (spanExpandCount == 0) {
                                    minWidths[j] += w;
                                } else {
                                    int delta = w / spanExpandCount;
                                    int remainder = w % spanExpandCount, last = -1;
                                    for (int k = 0; k < hSpan; k++) {
                                        if (expandColumn[j - k]) {
                                            minWidths[last = j - k] += delta;
                                        }
                                    }
                                    if (last > -1)
                                        minWidths[last] += remainder;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (getApi().makeColumnsEqualWidth) {
            int minColumnWidth = 0;
            int columnWidth = 0;
            for (int i = 0; i < columnCount; i++) {
                minColumnWidth = Math.max(minColumnWidth, minWidths[i]);
                columnWidth = Math.max(columnWidth, widths[i]);
            }
            columnWidth = width == SWT.DEFAULT || expandCount == 0 ? columnWidth : Math.max(minColumnWidth, availableWidth / columnCount);
            for (int i = 0; i < columnCount; i++) {
                expandColumn[i] = expandCount > 0;
                widths[i] = columnWidth;
            }
        } else {
            if (width != SWT.DEFAULT && expandCount > 0) {
                int totalWidth = 0;
                for (int i = 0; i < columnCount; i++) {
                    totalWidth += widths[i];
                }
                int c = expandCount;
                int delta = (availableWidth - totalWidth) / c;
                int remainder = (availableWidth - totalWidth) % c;
                int last = -1;
                while (totalWidth != availableWidth) {
                    for (int j = 0; j < columnCount; j++) {
                        if (expandColumn[j]) {
                            if (widths[j] + delta > minWidths[j]) {
                                widths[last = j] = widths[j] + delta;
                            } else {
                                widths[j] = minWidths[j];
                                expandColumn[j] = false;
                                c--;
                            }
                        }
                    }
                    if (last > -1)
                        widths[last] += remainder;
                    for (int j = 0; j < columnCount; j++) {
                        for (int i = 0; i < rowCount; i++) {
                            GridData data = getData(grid, i, j, rowCount, columnCount, false);
                            if (data != null) {
                                int hSpan = Math.max(1, Math.min(data.horizontalSpan, columnCount));
                                if (hSpan > 1) {
                                    if (!data.grabExcessHorizontalSpace || data.minimumWidth != 0) {
                                        int spanWidth = 0, spanExpandCount = 0;
                                        for (int k = 0; k < hSpan; k++) {
                                            spanWidth += widths[j - k];
                                            if (expandColumn[j - k])
                                                spanExpandCount++;
                                        }
                                        int w = !data.grabExcessHorizontalSpace || data.minimumWidth == SWT.DEFAULT ? ((SwtGridData) data.getImpl()).cacheWidth : data.minimumWidth;
                                        w += data.horizontalIndent - spanWidth - (hSpan - 1) * getApi().horizontalSpacing;
                                        if (w > 0) {
                                            if (spanExpandCount == 0) {
                                                widths[j] += w;
                                            } else {
                                                int delta2 = w / spanExpandCount;
                                                int remainder2 = w % spanExpandCount, last2 = -1;
                                                for (int k = 0; k < hSpan; k++) {
                                                    if (expandColumn[j - k]) {
                                                        widths[last2 = j - k] += delta2;
                                                    }
                                                }
                                                if (last2 > -1)
                                                    widths[last2] += remainder2;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (c == 0)
                        break;
                    totalWidth = 0;
                    for (int i = 0; i < columnCount; i++) {
                        totalWidth += widths[i];
                    }
                    delta = (availableWidth - totalWidth) / c;
                    remainder = (availableWidth - totalWidth) % c;
                    last = -1;
                }
            }
        }
        /* Wrapping */
        GridData[] flush = null;
        int flushLength = 0;
        if (width != SWT.DEFAULT) {
            for (int j = 0; j < columnCount; j++) {
                for (int i = 0; i < rowCount; i++) {
                    GridData data = getData(grid, i, j, rowCount, columnCount, false);
                    if (data != null) {
                        if (data.heightHint == SWT.DEFAULT) {
                            Control child = grid[i][j];
                            //TEMPORARY CODE
                            int hSpan = Math.max(1, Math.min(data.horizontalSpan, columnCount));
                            int currentWidth = 0;
                            for (int k = 0; k < hSpan; k++) {
                                currentWidth += widths[j - k];
                            }
                            currentWidth += (hSpan - 1) * getApi().horizontalSpacing - data.horizontalIndent;
                            if ((currentWidth != ((SwtGridData) data.getImpl()).cacheWidth && data.horizontalAlignment == SWT.FILL) || (((SwtGridData) data.getImpl()).cacheWidth > currentWidth)) {
                                int trim = 0;
                                if (child instanceof Scrollable) {
                                    Rectangle rect = ((Scrollable) child).computeTrim(0, 0, 0, 0);
                                    trim = rect.width;
                                } else {
                                    trim = child.getBorderWidth() * 2;
                                }
                                ((SwtGridData) data.getImpl()).cacheWidth = ((SwtGridData) data.getImpl()).cacheHeight = SWT.DEFAULT;
                                ((SwtGridData) data.getImpl()).computeSize(child, Math.max(0, currentWidth - trim), data.heightHint, false);
                                if (data.grabExcessVerticalSpace && data.minimumHeight > 0) {
                                    ((SwtGridData) data.getImpl()).cacheHeight = Math.max(((SwtGridData) data.getImpl()).cacheHeight, data.minimumHeight);
                                }
                                if (flush == null)
                                    flush = new GridData[count];
                                flush[flushLength++] = data;
                            }
                        }
                    }
                }
            }
        }
        /* Row heights */
        int availableHeight = height - getApi().verticalSpacing * (rowCount - 1) - (getApi().marginTop + getApi().marginHeight * 2 + getApi().marginBottom);
        expandCount = 0;
        int[] heights = new int[rowCount];
        int[] minHeights = new int[rowCount];
        boolean[] expandRow = new boolean[rowCount];
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                GridData data = getData(grid, i, j, rowCount, columnCount, true);
                if (data != null) {
                    int vSpan = Math.max(1, Math.min(data.verticalSpan, rowCount));
                    if (vSpan == 1) {
                        int h = ((SwtGridData) data.getImpl()).cacheHeight + data.verticalIndent;
                        heights[i] = Math.max(heights[i], h);
                        if (data.grabExcessVerticalSpace) {
                            if (!expandRow[i])
                                expandCount++;
                            expandRow[i] = true;
                        }
                        if (!data.grabExcessVerticalSpace || data.minimumHeight != 0) {
                            h = !data.grabExcessVerticalSpace || data.minimumHeight == SWT.DEFAULT ? ((SwtGridData) data.getImpl()).cacheHeight : data.minimumHeight;
                            h += data.verticalIndent;
                            minHeights[i] = Math.max(minHeights[i], h);
                        }
                    }
                }
            }
            for (int j = 0; j < columnCount; j++) {
                GridData data = getData(grid, i, j, rowCount, columnCount, false);
                if (data != null) {
                    int vSpan = Math.max(1, Math.min(data.verticalSpan, rowCount));
                    if (vSpan > 1) {
                        int spanHeight = 0, spanMinHeight = 0, spanExpandCount = 0;
                        for (int k = 0; k < vSpan; k++) {
                            spanHeight += heights[i - k];
                            spanMinHeight += minHeights[i - k];
                            if (expandRow[i - k])
                                spanExpandCount++;
                        }
                        if (data.grabExcessVerticalSpace && spanExpandCount == 0) {
                            expandCount++;
                            expandRow[i] = true;
                        }
                        int h = ((SwtGridData) data.getImpl()).cacheHeight + data.verticalIndent - spanHeight - (vSpan - 1) * getApi().verticalSpacing;
                        if (h > 0) {
                            if (spanExpandCount == 0) {
                                heights[i] += h;
                            } else {
                                int delta = h / spanExpandCount;
                                int remainder = h % spanExpandCount, last = -1;
                                for (int k = 0; k < vSpan; k++) {
                                    if (expandRow[i - k]) {
                                        heights[last = i - k] += delta;
                                    }
                                }
                                if (last > -1)
                                    heights[last] += remainder;
                            }
                        }
                        if (!data.grabExcessVerticalSpace || data.minimumHeight != 0) {
                            h = !data.grabExcessVerticalSpace || data.minimumHeight == SWT.DEFAULT ? ((SwtGridData) data.getImpl()).cacheHeight : data.minimumHeight;
                            h += data.verticalIndent - spanMinHeight - (vSpan - 1) * getApi().verticalSpacing;
                            if (h > 0) {
                                if (spanExpandCount == 0) {
                                    minHeights[i] += h;
                                } else {
                                    int delta = h / spanExpandCount;
                                    int remainder = h % spanExpandCount, last = -1;
                                    for (int k = 0; k < vSpan; k++) {
                                        if (expandRow[i - k]) {
                                            minHeights[last = i - k] += delta;
                                        }
                                    }
                                    if (last > -1)
                                        minHeights[last] += remainder;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (height != SWT.DEFAULT && expandCount > 0) {
            int totalHeight = 0;
            for (int i = 0; i < rowCount; i++) {
                totalHeight += heights[i];
            }
            int c = expandCount;
            int delta = (availableHeight - totalHeight) / c;
            int remainder = (availableHeight - totalHeight) % c;
            int last = -1;
            while (totalHeight != availableHeight) {
                for (int i = 0; i < rowCount; i++) {
                    if (expandRow[i]) {
                        if (heights[i] + delta > minHeights[i]) {
                            heights[last = i] = heights[i] + delta;
                        } else {
                            heights[i] = minHeights[i];
                            expandRow[i] = false;
                            c--;
                        }
                    }
                }
                if (last > -1)
                    heights[last] += remainder;
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < columnCount; j++) {
                        GridData data = getData(grid, i, j, rowCount, columnCount, false);
                        if (data != null) {
                            int vSpan = Math.max(1, Math.min(data.verticalSpan, rowCount));
                            if (vSpan > 1) {
                                if (!data.grabExcessVerticalSpace || data.minimumHeight != 0) {
                                    int spanHeight = 0, spanExpandCount = 0;
                                    for (int k = 0; k < vSpan; k++) {
                                        spanHeight += heights[i - k];
                                        if (expandRow[i - k])
                                            spanExpandCount++;
                                    }
                                    int h = !data.grabExcessVerticalSpace || data.minimumHeight == SWT.DEFAULT ? ((SwtGridData) data.getImpl()).cacheHeight : data.minimumHeight;
                                    h += data.verticalIndent - spanHeight - (vSpan - 1) * getApi().verticalSpacing;
                                    if (h > 0) {
                                        if (spanExpandCount == 0) {
                                            heights[i] += h;
                                        } else {
                                            int delta2 = h / spanExpandCount;
                                            int remainder2 = h % spanExpandCount, last2 = -1;
                                            for (int k = 0; k < vSpan; k++) {
                                                if (expandRow[i - k]) {
                                                    heights[last2 = i - k] += delta2;
                                                }
                                            }
                                            if (last2 > -1)
                                                heights[last2] += remainder2;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (c == 0)
                    break;
                totalHeight = 0;
                for (int i = 0; i < rowCount; i++) {
                    totalHeight += heights[i];
                }
                delta = (availableHeight - totalHeight) / c;
                remainder = (availableHeight - totalHeight) % c;
                last = -1;
            }
        }
        /* Position the controls */
        if (move) {
            int gridY = y + getApi().marginTop + getApi().marginHeight;
            for (int i = 0; i < rowCount; i++) {
                int gridX = x + getApi().marginLeft + getApi().marginWidth;
                for (int j = 0; j < columnCount; j++) {
                    GridData data = getData(grid, i, j, rowCount, columnCount, true);
                    if (data != null) {
                        int hSpan = Math.max(1, Math.min(data.horizontalSpan, columnCount));
                        int vSpan = Math.max(1, data.verticalSpan);
                        int cellWidth = 0, cellHeight = 0;
                        for (int k = 0; k < hSpan; k++) {
                            cellWidth += widths[j + k];
                        }
                        for (int k = 0; k < vSpan; k++) {
                            cellHeight += heights[i + k];
                        }
                        cellWidth += getApi().horizontalSpacing * (hSpan - 1);
                        int childX = gridX + data.horizontalIndent;
                        int childWidth = Math.min(((SwtGridData) data.getImpl()).cacheWidth, cellWidth);
                        switch(data.horizontalAlignment) {
                            case SWT.CENTER:
                            case GridData.CENTER:
                                childX += Math.max(0, (cellWidth - data.horizontalIndent - childWidth) / 2);
                                break;
                            case SWT.RIGHT:
                            case SWT.END:
                            case GridData.END:
                                childX += Math.max(0, cellWidth - data.horizontalIndent - childWidth);
                                break;
                            case SWT.FILL:
                                childWidth = cellWidth - data.horizontalIndent;
                                break;
                        }
                        cellHeight += getApi().verticalSpacing * (vSpan - 1);
                        int childY = gridY + data.verticalIndent;
                        int childHeight = Math.min(((SwtGridData) data.getImpl()).cacheHeight, cellHeight);
                        switch(data.verticalAlignment) {
                            case SWT.CENTER:
                            case GridData.CENTER:
                                childY += Math.max(0, (cellHeight - data.verticalIndent - childHeight) / 2);
                                break;
                            case SWT.BOTTOM:
                            case SWT.END:
                            case GridData.END:
                                childY += Math.max(0, cellHeight - data.verticalIndent - childHeight);
                                break;
                            case SWT.FILL:
                                childHeight = cellHeight - data.verticalIndent;
                                break;
                        }
                        Control child = grid[i][j];
                        if (child != null) {
                            child.setBounds(childX, childY, childWidth, childHeight);
                        }
                    }
                    gridX += widths[j] + getApi().horizontalSpacing;
                }
                gridY += heights[i] + getApi().verticalSpacing;
            }
        }
        // clean up cache
        for (int i = 0; i < flushLength; i++) {
            ((SwtGridData) flush[i].getImpl()).cacheWidth = ((SwtGridData) flush[i].getImpl()).cacheHeight = -1;
        }
        int totalDefaultWidth = 0;
        int totalDefaultHeight = 0;
        for (int i = 0; i < columnCount; i++) {
            totalDefaultWidth += widths[i];
        }
        for (int i = 0; i < rowCount; i++) {
            totalDefaultHeight += heights[i];
        }
        totalDefaultWidth += getApi().horizontalSpacing * (columnCount - 1) + getApi().marginLeft + getApi().marginWidth * 2 + getApi().marginRight;
        totalDefaultHeight += getApi().verticalSpacing * (rowCount - 1) + getApi().marginTop + getApi().marginHeight * 2 + getApi().marginBottom;
        return new Point(totalDefaultWidth, totalDefaultHeight);
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
     * @return a string representation of the layout
     */
    @Override
    public String toString() {
        String string = getName() + " {";
        if (getApi().numColumns != 1)
            string += "numColumns=" + getApi().numColumns + " ";
        if (getApi().makeColumnsEqualWidth)
            string += "makeColumnsEqualWidth=" + getApi().makeColumnsEqualWidth + " ";
        if (getApi().marginWidth != 0)
            string += "marginWidth=" + getApi().marginWidth + " ";
        if (getApi().marginHeight != 0)
            string += "marginHeight=" + getApi().marginHeight + " ";
        if (getApi().marginLeft != 0)
            string += "marginLeft=" + getApi().marginLeft + " ";
        if (getApi().marginRight != 0)
            string += "marginRight=" + getApi().marginRight + " ";
        if (getApi().marginTop != 0)
            string += "marginTop=" + getApi().marginTop + " ";
        if (getApi().marginBottom != 0)
            string += "marginBottom=" + getApi().marginBottom + " ";
        if (getApi().horizontalSpacing != 0)
            string += "horizontalSpacing=" + getApi().horizontalSpacing + " ";
        if (getApi().verticalSpacing != 0)
            string += "verticalSpacing=" + getApi().verticalSpacing + " ";
        string = string.trim();
        string += "}";
        return string;
    }

    public GridLayout getApi() {
        if (api == null)
            api = GridLayout.createApi(this);
        return (GridLayout) api;
    }
}
