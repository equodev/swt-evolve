package org.eclipse.swt.widgets;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.custom.*;

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
        int kept = 0;
        for (TreeColumn v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TreeColumn[] result = new TreeColumn[kept];
        kept = 0;
        for (TreeColumn v : values) if (v != null)
            result[kept++] = v;
        return result;
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
        return ((DartTree) impl).headerBackground;
    }

    public void setHeaderBackground(Color value) {
        ((DartTree) impl).headerBackground = value;
    }

    public Color getHeaderForeground() {
        return ((DartTree) impl).headerForeground;
    }

    public void setHeaderForeground(Color value) {
        ((DartTree) impl).headerForeground = value;
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
        int kept = 0;
        for (TreeItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TreeItem[] result = new TreeItem[kept];
        kept = 0;
        for (TreeItem v : values) if (v != null)
            result[kept++] = v;
        return result;
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
        int kept = 0;
        for (TreeItem v : values) if (v != null)
            kept++;
        if (kept == values.length)
            return values;
        TreeItem[] result = new TreeItem[kept];
        kept = 0;
        for (TreeItem v : values) if (v != null)
            result[kept++] = v;
        return result;
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

    public static final int COLUMN_ORDER = 637;

    public static final int COLUMNS = 638;

    public static final int EDITORS = 639;

    public static final int HEADER_BACKGROUND = 640;

    public static final int HEADER_FOREGROUND = 641;

    public static final int HEADER_VISIBLE = 642;

    public static final int ITEMS = 643;

    public static final int LINES_VISIBLE = 644;

    public static final int OWNER_DRAW_ITEM_HEIGHT = 645;

    public static final int SELECTION = 646;

    public static final int SORT_COLUMN = 647;

    public static final int SORT_DIRECTION = 648;

    public static final int TOP_ITEM = 649;

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
            case 149:
                return false;
            case 637:
                w.write(getColumnOrder());
                return true;
            case 638:
                w.refs(getColumns());
                return true;
            case 639:
                w.refs(getEditors());
                return true;
            case 640:
                w.write(getHeaderBackground());
                return true;
            case 641:
                w.write(getHeaderForeground());
                return true;
            case 642:
                w.write(getHeaderVisible());
                return true;
            case 643:
                w.items(getItems(), dev.equo.swt.WireTable.TYPE_TREE_ITEM);
                return true;
            case 644:
                w.write(getLinesVisible());
                return true;
            case 646:
                w.refs(getSelection());
                return true;
            case 647:
                w.ref(getSortColumn());
                return true;
            case 648:
                w.write((int) getSortDirection());
                return true;
            case 649:
                w.ref(getTopItem());
                return true;
            case 645:
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
            case 637:
                {
                    int[] v_ = getColumnOrder();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(637);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 638:
                {
                    TreeColumn[] v_ = getColumns();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(638);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 639:
                {
                    TreeEditor[] v_ = getEditors();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(639);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 640:
                {
                    Color v_ = getHeaderBackground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(640);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 641:
                {
                    Color v_ = getHeaderForeground();
                    if (v_ == null || v_.isDisposed())
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(641);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 642:
                {
                    boolean v_ = getHeaderVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(642);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 643:
                {
                    TreeItem[] v_ = getItems();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(643);
                    w.items(v_, dev.equo.swt.WireTable.TYPE_TREE_ITEM);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 644:
                {
                    boolean v_ = getLinesVisible();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(644);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 646:
                {
                    TreeItem[] v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(646);
                    w.refs(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 647:
                {
                    TreeColumn v_ = getSortColumn();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(647);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 648:
                {
                    int v_ = getSortDirection();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(648);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 649:
                {
                    TreeItem v_ = getTopItem();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(649);
                    w.ref(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 645:
                {
                    int v_ = getOwnerDrawItemHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(645);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VComposite.allWireKeys_(), new int[] { 637, 638, 639, 640, 641, 642, 643, 644, 646, 647, 648, 649, 645 });

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
