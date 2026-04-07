/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package dev.equo.nattable;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.SelectionStyleLabels;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class _000_Default_NatTable extends AbstractNatExample {

    public static void main(String[] args) throws Exception {
        StandaloneNatExampleRunner.run(800, 600, new _000_Default_NatTable());
    }

    private static void applyFlutterTheme(IConfigRegistry reg) {
        // Column header — Teal 600, white text
        Style colHeader = new Style();
        colHeader.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(0, 137, 123));
        colHeader.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.getColor(255, 255, 255));
        reg.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, colHeader, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);

        // Column header selected — Teal 800, white text
        Style colHeaderSel = new Style();
        colHeaderSel.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(0, 105, 92));
        colHeaderSel.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.getColor(255, 255, 255));
        reg.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, colHeaderSel, DisplayMode.SELECT, GridRegion.COLUMN_HEADER);

        // Row header — Teal 50
        Style rowHeader = new Style();
        rowHeader.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(224, 242, 241));
        reg.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, rowHeader, DisplayMode.NORMAL, GridRegion.ROW_HEADER);

        // Row header selected — Teal 100
        Style rowHeaderSel = new Style();
        rowHeaderSel.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(178, 223, 219));
        reg.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, rowHeaderSel, DisplayMode.SELECT, GridRegion.ROW_HEADER);

        // Corner — Teal 800, white text
        Style corner = new Style();
        corner.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(0, 105, 92));
        corner.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.getColor(255, 255, 255));
        reg.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, corner, DisplayMode.NORMAL, GridRegion.CORNER);

        // Body selected — Teal 100
        Style sel = new Style();
        sel.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(178, 223, 219));
        reg.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, sel, DisplayMode.SELECT, GridRegion.BODY);

        // Selection anchor — same Teal 100 (overrides the default gray)
        Style anchor = new Style();
        anchor.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(178, 223, 219));
        reg.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, anchor, DisplayMode.SELECT, SelectionStyleLabels.SELECTION_ANCHOR_STYLE);

        // Grid lines — light gray
        reg.registerConfigAttribute(CellConfigAttributes.GRID_LINE_COLOR, GUIHelper.getColor(224, 224, 224));
    }

    @Override
    public String getDescription() {
        return "The easiest NatTable instance to create (no arguments, other than the parent composite) builds this default example "
                + "table. A lot of functionality is available in it to try out. Here are some things you can do:\n"
                + "\n"
                + "* RESIZE COLUMNS/ROWS by clicking on a column/row boundary in the column/row header and dragging it.\n"
                + "* AUTO-RESIZE COLUMNS/ROWS by double-clicking on a column/row boundary.\n"
                + "\n"
                + "* REORDER COLUMNS by clicking on a column header and dragging it.\n"
                + "\n"
                + "* SELECT A CELL by clicking on it.\n"
                + "* SELECT A REGION OF CELLS by dragging.\n"
                + "* SELECT A COLUMN/ROW by clicking on a column/row header.\n"
                + "* ADD TO SELECTION using the ctrl and shift modifiers.\n"
                + "\n"
                + "* EDIT A SELECTED CELL by typing F2 or edit directly by typing alphanumeric content; if multiple cells with the same "
                + "editor type are selected, you can edit all of them at once.\n"
                + "\n"
                + "* FIND DATA in the body area of the table with ctrl-f.\n"
                + "* COPY SELECTED CELLS into the clipboard with ctrl-c.\n"
                + "* EXPORT AS EXCEL with ctrl-e.\n" + "* PRINT with ctrl-p.";
    }

    @Override
    public Control createExampleControl(Composite parent) {
        NatTable natTable = new NatTable(parent);
        applyFlutterTheme(natTable.getConfigRegistry());
        return natTable;
    }

}
