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
        return ((DartTable) impl)._itemCount();
    }

    public int getOwnerDrawItemHeight() {
        return ((DartTable) impl).wireOwnerDrawItemHeight();
    }

    public boolean getCompactRows() {
        return ((DartTable) impl).wireCompactRows();
    }

    public static final int COLUMN_ORDER = 5184;

    public static final int COLUMNS = 5185;

    public static final int COMPACT_ROWS = 5198;

    public static final int EDITORS = 5186;

    public static final int HEADER_BACKGROUND = 5187;

    public static final int HEADER_FOREGROUND = 5188;

    public static final int HEADER_VISIBLE = 5189;

    public static final int ITEM_COUNT = 5190;

    public static final int ITEMS = 5191;

    public static final int LINES_VISIBLE = 5192;

    public static final int OWNER_DRAW_ITEM_HEIGHT = 5193;

    public static final int SELECTION = 5194;

    public static final int SORT_COLUMN = 5196;

    public static final int SORT_DIRECTION = 5197;

    public static final int TOP_INDEX = 5195;

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
            case COMPACT_ROWS:
                return "compactRows";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1163:
                return false;
            case 5184:
                w.write(getColumnOrder());
                return true;
            case 5185:
                w.refs(getColumns());
                return true;
            case 5186:
                w.refs(getEditors());
                return true;
            case 5187:
                w.write(getHeaderBackground());
                return true;
            case 5188:
                w.write(getHeaderForeground());
                return true;
            case 5189:
                w.write(getHeaderVisible());
                return true;
            case 5191:
                w.items(getItems(), dev.equo.swt.WireTable.TYPE_TABLE_ITEM);
                return true;
            case 5192:
                w.write(getLinesVisible());
                return true;
            case 5194:
                w.write(getSelection());
                return true;
            case 5196:
                w.ref(getSortColumn());
                return true;
            case 5197:
                w.write((int) getSortDirection());
                return true;
            case 5195:
                w.write((int) getTopIndex());
                return true;
            case 5190:
                w.write((int) getItemCount());
                return true;
            case 5193:
                w.write((int) getOwnerDrawItemHeight());
                return true;
            case 5198:
                w.write(getCompactRows());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1163:
                return dev.equo.swt.WidgetWire.UNHANDLED;
            case 5184:
                {
                    int[] v_ = getColumnOrder();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(5184);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5185:
                {
                    TableColumn[] v_ = getColumns();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(5185);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5186:
                {
                    TableEditor[] v_ = getEditors();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(5186);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5187:
                {
                    Color v_ = getHeaderBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(5187);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5188:
                {
                    Color v_ = getHeaderForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(5188);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5189:
                {
                    boolean v_ = getHeaderVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(5189);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5191:
                {
                    TableItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(5191);
                    w.items(v_, dev.equo.swt.WireTable.TYPE_TABLE_ITEM);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5192:
                {
                    boolean v_ = getLinesVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(5192);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5194:
                {
                    int[] v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(5194);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5196:
                {
                    TableColumn v_ = getSortColumn();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(5196);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5197:
                {
                    int v_ = getSortDirection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(5197);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5195:
                {
                    int v_ = getTopIndex();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(5195);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5190:
                {
                    int v_ = getItemCount();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(5190);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5193:
                {
                    int v_ = getOwnerDrawItemHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(5193);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 5198:
                {
                    boolean v_ = getCompactRows();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(5198);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 5184, 5185, 5186, 5187, 5188, 5189, 5191, 5192, 5194, 5196, 5197, 5195, 5190, 5193, 5198 });

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
