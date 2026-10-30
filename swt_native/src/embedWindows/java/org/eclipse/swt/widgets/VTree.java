package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.custom.*;
import java.util.ArrayList;

public class VTree extends VComposite {

    protected VTree() {
    }

    protected VTree(DartTree impl) {
        super(impl);
    }

    public int[] getColumnOrder() {
        return ((DartTree) impl).columnOrder;
    }

    public void setColumnOrder(int[] value) {
        ((DartTree) impl).columnOrder = value;
    }

    public TreeColumn[] getColumns() {
        TreeColumn[] values = ((DartTree) impl).columns;
        if (values == null)
            return null;
        ArrayList<TreeColumn> result = new ArrayList<>(values.length);
        for (TreeColumn v : values) if (v != null)
            result.add(v);
        return result.toArray(new TreeColumn[0]);
    }

    public void setColumns(TreeColumn[] value) {
        ((DartTree) impl).columns = value;
    }

    public TreeEditor[] getEditors() {
        return ((DartTree) impl).editors;
    }

    public void setEditors(TreeEditor[] value) {
        ((DartTree) impl).editors = value;
    }

    public Color getHeaderBackground() {
        return ((DartTree) impl).getHeaderBackground();
    }

    public void setHeaderBackground(Color value) {
        ((DartTree) impl).setHeaderBackground(value);
    }

    public Color getHeaderForeground() {
        return ((DartTree) impl).getHeaderForeground();
    }

    public void setHeaderForeground(Color value) {
        ((DartTree) impl).setHeaderForeground(value);
    }

    public boolean getHeaderVisible() {
        return ((DartTree) impl).getHeaderVisible();
    }

    public void setHeaderVisible(boolean value) {
        ((DartTree) impl).headerVisible = value;
    }

    public TreeItem[] getItems() {
        TreeItem[] values = ((DartTree) impl).items;
        if (values == null)
            return null;
        ArrayList<TreeItem> result = new ArrayList<>(values.length);
        for (TreeItem v : values) if (v != null)
            result.add(v);
        return result.toArray(new TreeItem[0]);
    }

    public void setItems(TreeItem[] value) {
        ((DartTree) impl).items = value;
    }

    public boolean getLinesVisible() {
        return ((DartTree) impl).getLinesVisible();
    }

    public void setLinesVisible(boolean value) {
        ((DartTree) impl).linesVisible = value;
    }

    public TreeItem[] getSelection() {
        TreeItem[] values = ((DartTree) impl).selection;
        if (values == null)
            return null;
        ArrayList<TreeItem> result = new ArrayList<>(values.length);
        for (TreeItem v : values) if (v != null)
            result.add(v);
        return result.toArray(new TreeItem[0]);
    }

    public void setSelection(TreeItem[] value) {
        ((DartTree) impl).selection = value;
    }

    public TreeColumn getSortColumn() {
        TreeColumn val = ((DartTree) impl).sortColumn;
        if (val != null && !(val.getImpl() instanceof DartTreeColumn))
            return null;
        return val;
    }

    public void setSortColumn(TreeColumn value) {
        ((DartTree) impl).sortColumn = value;
    }

    public int getSortDirection() {
        return ((DartTree) impl).getSortDirection();
    }

    public void setSortDirection(int value) {
        ((DartTree) impl).sortDirection = value;
    }

    public TreeItem getTopItem() {
        TreeItem val = ((DartTree) impl).topItem;
        if (val != null && !(val.getImpl() instanceof DartTreeItem))
            return null;
        return val;
    }

    public void setTopItem(TreeItem value) {
        ((DartTree) impl).topItem = value;
    }

    public int getOwnerDrawItemHeight() {
        return ((DartTree) impl).wireOwnerDrawItemHeight();
    }

    public static final int COLUMN_ORDER = 1383;

    public static final int COLUMNS = 454;

    public static final int EDITORS = 455;

    public static final int HEADER_BACKGROUND = 456;

    public static final int HEADER_FOREGROUND = 1384;

    public static final int HEADER_VISIBLE = 457;

    public static final int ITEMS = 458;

    public static final int LINES_VISIBLE = 459;

    public static final int OWNER_DRAW_ITEM_HEIGHT = 1421;

    public static final int SELECTION = 460;

    public static final int SORT_COLUMN = 1385;

    public static final int SORT_DIRECTION = 1386;

    public static final int TOP_ITEM = 1387;

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
            case TOP_ITEM:
                return "topItem";
            case OWNER_DRAW_ITEM_HEIGHT:
                return "ownerDrawItemHeight";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1383:
                w.write(getColumnOrder());
                return true;
            case 454:
                w.refs(getColumns());
                return true;
            case 455:
                w.refs(getEditors());
                return true;
            case 456:
                w.write(getHeaderBackground());
                return true;
            case 1384:
                w.write(getHeaderForeground());
                return true;
            case 457:
                w.write(getHeaderVisible());
                return true;
            case 458:
                w.refs(getItems());
                return true;
            case 459:
                w.write(getLinesVisible());
                return true;
            case 460:
                w.refs(getSelection());
                return true;
            case 1385:
                w.ref(getSortColumn());
                return true;
            case 1386:
                w.write((int) getSortDirection());
                return true;
            case 1387:
                w.ref(getTopItem());
                return true;
            case 1421:
                w.write((int) getOwnerDrawItemHeight());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 1383:
                {
                    int[] v_ = getColumnOrder();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(1383);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 454:
                {
                    TreeColumn[] v_ = getColumns();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(454);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 455:
                {
                    TreeEditor[] v_ = getEditors();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(455);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 456:
                {
                    Color v_ = getHeaderBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(456);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1384:
                {
                    Color v_ = getHeaderForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1384);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 457:
                {
                    boolean v_ = getHeaderVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(457);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 458:
                {
                    TreeItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(458);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 459:
                {
                    boolean v_ = getLinesVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(459);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 460:
                {
                    TreeItem[] v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(460);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1385:
                {
                    TreeColumn v_ = getSortColumn();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1385);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1386:
                {
                    int v_ = getSortDirection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1386);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1387:
                {
                    TreeItem v_ = getTopItem();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1387);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 1421:
                {
                    int v_ = getOwnerDrawItemHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(1421);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 1383, 454, 455, 456, 1384, 457, 458, 459, 460, 1385, 1386, 1387, 1421 });

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
