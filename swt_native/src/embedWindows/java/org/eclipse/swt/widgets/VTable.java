package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.custom.*;

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
        int kept = 0;
        for (TableColumn v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TableColumn[] result = new TableColumn[kept];
        kept = 0;
        for (TableColumn v : values) if (v != null)
            result[kept++] = v;
        return result;
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
        return ((DartTable) impl).getHeaderBackground();
    }

    public void setHeaderBackground(Color value) {
        ((DartTable) impl).setHeaderBackground(value);
    }

    public Color getHeaderForeground() {
        return ((DartTable) impl).getHeaderForeground();
    }

    public void setHeaderForeground(Color value) {
        ((DartTable) impl).setHeaderForeground(value);
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
        int kept = 0;
        for (TableItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TableItem[] result = new TableItem[kept];
        kept = 0;
        for (TableItem v : values) if (v != null)
            result[kept++] = v;
        return result;
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
        return ((DartTable) impl).getItemCount();
    }

    public int getOwnerDrawItemHeight() {
        return ((DartTable) impl).wireOwnerDrawItemHeight();
    }

    public static final int COLUMN_ORDER = 559;

    public static final int COLUMNS = 560;

    public static final int EDITORS = 561;

    public static final int HEADER_BACKGROUND = 562;

    public static final int HEADER_FOREGROUND = 563;

    public static final int HEADER_VISIBLE = 564;

    public static final int ITEM_COUNT = 565;

    public static final int ITEMS = 566;

    public static final int LINES_VISIBLE = 567;

    public static final int OWNER_DRAW_ITEM_HEIGHT = 568;

    public static final int SELECTION = 569;

    public static final int SORT_COLUMN = 570;

    public static final int SORT_DIRECTION = 571;

    public static final int TOP_INDEX = 572;

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
            case SORT_COLUMN:
                return "sortColumn";
            case SORT_DIRECTION:
                return "sortDirection";
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
            case 149:
                return false;
            case 559:
                w.write(getColumnOrder());
                return true;
            case 560:
                w.refs(getColumns());
                return true;
            case 561:
                w.refs(getEditors());
                return true;
            case 562:
                w.write(getHeaderBackground());
                return true;
            case 563:
                w.write(getHeaderForeground());
                return true;
            case 564:
                w.write(getHeaderVisible());
                return true;
            case 566:
                w.items(getItems(), dev.equo.swt.WireTable.TYPE_TABLE_ITEM);
                return true;
            case 567:
                w.write(getLinesVisible());
                return true;
            case 569:
                w.write(getSelection());
                return true;
            case 570:
                w.ref(getSortColumn());
                return true;
            case 571:
                w.write((int) getSortDirection());
                return true;
            case 572:
                w.write((int) getTopIndex());
                return true;
            case 565:
                w.write((int) getItemCount());
                return true;
            case 568:
                w.write((int) getOwnerDrawItemHeight());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 149:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 559:
                {
                    int[] v_ = getColumnOrder();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(559);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 560:
                {
                    TableColumn[] v_ = getColumns();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(560);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 561:
                {
                    TableEditor[] v_ = getEditors();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(561);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 562:
                {
                    Color v_ = getHeaderBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(562);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 563:
                {
                    Color v_ = getHeaderForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(563);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 564:
                {
                    boolean v_ = getHeaderVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(564);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 566:
                {
                    TableItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(566);
                    w.items(v_, dev.equo.swt.WireTable.TYPE_TABLE_ITEM);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 567:
                {
                    boolean v_ = getLinesVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(567);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 569:
                {
                    int[] v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(569);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 570:
                {
                    TableColumn v_ = getSortColumn();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(570);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 571:
                {
                    int v_ = getSortDirection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(571);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 572:
                {
                    int v_ = getTopIndex();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(572);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 565:
                {
                    int v_ = getItemCount();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(565);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 568:
                {
                    int v_ = getOwnerDrawItemHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(568);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 559, 560, 561, 562, 563, 564, 566, 567, 569, 570, 571, 572, 565, 568 });

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
