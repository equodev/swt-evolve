package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.custom.*;
import java.util.ArrayList;

public class VTable extends VComposite {

    protected VTable() {
    }

    protected VTable(DartTable impl) {
        super(impl);
    }

    public int[] getColumnOrder() {
        return ((DartTable) impl).columnOrder;
    }

    public void setColumnOrder(int[] value) {
        ((DartTable) impl).columnOrder = value;
    }

    public TableColumn[] getColumns() {
        TableColumn[] values = ((DartTable) impl).columns;
        if (values == null)
            return null;
        ArrayList<TableColumn> result = new ArrayList<>(values.length);
        for (TableColumn v : values) if (v != null)
            result.add(v);
        return result.toArray(new TableColumn[0]);
    }

    public void setColumns(TableColumn[] value) {
        ((DartTable) impl).columns = value;
    }

    public TableEditor[] getEditors() {
        return ((DartTable) impl)._editors();
    }

    public void setEditors(TableEditor[] value) {
        ((DartTable) impl).editors = value;
    }

    public Color getHeaderBackground() {
        return ((DartTable) impl).headerBackground;
    }

    public void setHeaderBackground(Color value) {
        ((DartTable) impl).headerBackground = value;
    }

    public Color getHeaderForeground() {
        return ((DartTable) impl).headerForeground;
    }

    public void setHeaderForeground(Color value) {
        ((DartTable) impl).headerForeground = value;
    }

    public boolean getHeaderVisible() {
        return ((DartTable) impl).getHeaderVisible();
    }

    public void setHeaderVisible(boolean value) {
        ((DartTable) impl).headerVisible = value;
    }

    public TableItem[] getItems() {
        TableItem[] values = ((DartTable) impl).items;
        if (values == null)
            return null;
        ArrayList<TableItem> result = new ArrayList<>(values.length);
        for (TableItem v : values) if (v != null)
            result.add(v);
        return result.toArray(new TableItem[0]);
    }

    public void setItems(TableItem[] value) {
        ((DartTable) impl).items = value;
    }

    public boolean getLinesVisible() {
        return ((DartTable) impl).getLinesVisible();
    }

    public void setLinesVisible(boolean value) {
        ((DartTable) impl).linesVisible = value;
    }

    public int[] getSelection() {
        return ((DartTable) impl).selection;
    }

    public void setSelection(int[] value) {
        ((DartTable) impl).selection = value;
    }

    public TableColumn getSortColumn() {
        TableColumn val = ((DartTable) impl).sortColumn;
        if (val != null && !(val.getImpl() instanceof DartTableColumn))
            return null;
        return val;
    }

    public void setSortColumn(TableColumn value) {
        ((DartTable) impl).sortColumn = value;
    }

    public int getSortDirection() {
        return ((DartTable) impl).getSortDirection();
    }

    public void setSortDirection(int value) {
        ((DartTable) impl).sortDirection = value;
    }

    public int getTopIndex() {
        return ((DartTable) impl).getTopIndex();
    }

    public void setTopIndex(int value) {
        ((DartTable) impl).topIndex = value;
    }

    public int getItemCount() {
        return ((DartTable) impl)._itemCount();
    }

    public int getOwnerDrawItemHeight() {
        return ((DartTable) impl).wireOwnerDrawItemHeight();
    }

    public static final int COLUMN_ORDER = 1388;

    public static final int COLUMNS = 465;

    public static final int EDITORS = 466;

    public static final int HEADER_BACKGROUND = 467;

    public static final int HEADER_FOREGROUND = 468;

    public static final int HEADER_VISIBLE = 469;

    public static final int ITEM_COUNT = 470;

    public static final int ITEMS = 471;

    public static final int LINES_VISIBLE = 472;

    public static final int OWNER_DRAW_ITEM_HEIGHT = 1423;

    public static final int SELECTION = 473;

    public static final int TOP_INDEX = 1353;

    @Override
    public String wireName(int key) {
        switch(key) {
            case COLUMN_ORDER:
                return "columnOrder";
            case COLUMNS:
                return "columns";
            case EDITORS:
                return "editors";
            case HEADER_BACKGROUND:
                return "headerBackground";
            case HEADER_FOREGROUND:
                return "headerForeground";
            case HEADER_VISIBLE:
                return "headerVisible";
            case ITEMS:
                return "items";
            case LINES_VISIBLE:
                return "linesVisible";
            case SELECTION:
                return "selection";
            case TOP_INDEX:
                return "topIndex";
            case ITEM_COUNT:
                return "itemCount";
            case OWNER_DRAW_ITEM_HEIGHT:
                return "ownerDrawItemHeight";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1388:
                w.write(getColumnOrder());
                return true;
            case 465:
                w.refs(getColumns());
                return true;
            case 466:
                w.refs(getEditors());
                return true;
            case 467:
                w.write(getHeaderBackground());
                return true;
            case 468:
                w.write(getHeaderForeground());
                return true;
            case 469:
                w.write(getHeaderVisible());
                return true;
            case 471:
                w.refs(getItems());
                return true;
            case 472:
                w.write(getLinesVisible());
                return true;
            case 473:
                w.write(getSelection());
                return true;
            case 1353:
                w.write((int) getTopIndex());
                return true;
            case 470:
                w.write((int) getItemCount());
                return true;
            case 1423:
                w.write((int) getOwnerDrawItemHeight());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1388:
                {
                    int[] v_ = getColumnOrder();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(1388);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 465:
                {
                    TableColumn[] v_ = getColumns();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(465);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 466:
                {
                    TableEditor[] v_ = getEditors();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(466);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 467:
                {
                    Color v_ = getHeaderBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(467);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 468:
                {
                    Color v_ = getHeaderForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(468);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 469:
                {
                    boolean v_ = getHeaderVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(469);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 471:
                {
                    TableItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(471);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 472:
                {
                    boolean v_ = getLinesVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(472);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 473:
                {
                    int[] v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(473);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1353:
                {
                    int v_ = getTopIndex();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1353);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 470:
                {
                    int v_ = getItemCount();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(470);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1423:
                {
                    int v_ = getOwnerDrawItemHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1423);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 1388, 465, 466, 467, 468, 469, 471, 472, 473, 1353, 470, 1423 });

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
