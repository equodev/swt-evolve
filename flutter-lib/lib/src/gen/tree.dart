import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/composite.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/scrollbar.dart';
import '../gen/treecolumn.dart';
import '../gen/treeeditor.dart';
import '../gen/treeitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/tree_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class TreeSwt<V extends VTree> extends CompositeSwt<V> {
  const TreeSwt({super.key, required super.value});

  @override
  State createState() => TreeImpl<TreeSwt<VTree>, VTree>();

  void sendModifyModify(V val, VEvent? payload) {
    sendEvent(val, 65, payload); // Modify/Modify
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
  }

  void sendSetDataSetData(V val, VEvent? payload) {
    sendEvent(val, 89, payload); // SetData/SetData
  }

  void sendTreeCollapse(V val, VEvent? payload) {
    sendEvent(val, 105, payload); // Tree/Collapse
  }

  void sendTreeExpand(V val, VEvent? payload) {
    sendEvent(val, 106, payload); // Tree/Expand
  }
}

class VTree extends VComposite {
  VTree() : this.empty();
  VTree.empty() {
    swt = "Tree";
  }

  List<int>? columnOrder;
  List<VTreeColumn>? columns;
  List<VTreeEditor>? editors;
  VColor? headerBackground;
  VColor? headerForeground;
  bool? headerVisible;
  List<VTreeItem>? items;
  bool? linesVisible;
  List<VTreeItem>? selection;
  VTreeColumn? sortColumn;
  int? sortDirection;
  VTreeItem? topItem;

  int? ownerDrawItemHeight;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTree) {
      columnOrder = other.columnOrder;
      columns = other.columns;
      editors = other.editors;
      headerBackground = other.headerBackground;
      headerForeground = other.headerForeground;
      headerVisible = other.headerVisible;
      items = other.items;
      linesVisible = other.linesVisible;
      selection = other.selection;
      sortColumn = other.sortColumn;
      sortDirection = other.sortDirection;
      topItem = other.topItem;
      ownerDrawItemHeight = other.ownerDrawItemHeight;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 622: // columnOrder
        columnOrder = r.ints();
        return true;
      case 623: // columns
        columns = r.refs<VTreeColumn>();
        return true;
      case 624: // editors
        editors = r.refs<VTreeEditor>();
        return true;
      case 625: // headerBackground
        headerBackground = r.color();
        return true;
      case 626: // headerForeground
        headerForeground = r.color();
        return true;
      case 627: // headerVisible
        headerVisible = r.boolean();
        return true;
      case 628: // items
        items = r.refs<VTreeItem>();
        return true;
      case 629: // linesVisible
        linesVisible = r.boolean();
        return true;
      case 631: // selection
        selection = r.refs<VTreeItem>();
        return true;
      case 632: // sortColumn
        sortColumn = r.ref<VTreeColumn>();
        return true;
      case 633: // sortDirection
        sortDirection = r.int32();
        return true;
      case 634: // topItem
        topItem = r.ref<VTreeItem>();
        return true;
      case 630: // ownerDrawItemHeight
        ownerDrawItemHeight = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = columnOrder;
      if (v_ != null) {
        w.key(622);
        w.ints(v_);
      }
    }
    {
      final v_ = columns;
      if (v_ != null) {
        w.key(623);
        w.refs(v_);
      }
    }
    {
      final v_ = editors;
      if (v_ != null) {
        w.key(624);
        w.refs(v_);
      }
    }
    {
      final v_ = headerBackground;
      if (v_ != null) {
        w.key(625);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerForeground;
      if (v_ != null) {
        w.key(626);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerVisible;
      if (v_ != null) {
        w.keyBoolean(627, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(628);
        w.refs(v_);
      }
    }
    {
      final v_ = linesVisible;
      if (v_ != null) {
        w.keyBoolean(629, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(631);
        w.refs(v_);
      }
    }
    {
      final v_ = sortColumn;
      if (v_ != null) {
        w.keyRef(632, v_.id);
      }
    }
    {
      final v_ = sortDirection;
      if (v_ != null) {
        w.keyInt32(633, v_);
      }
    }
    {
      final v_ = topItem;
      if (v_ != null) {
        w.keyRef(634, v_.id);
      }
    }
    {
      final v_ = ownerDrawItemHeight;
      if (v_ != null) {
        w.keyInt32(630, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(622, columnOrder);
    visit(623, columns);
    visit(624, editors);
    visit(625, headerBackground);
    visit(626, headerForeground);
    visit(627, headerVisible);
    visit(628, items);
    visit(629, linesVisible);
    visit(631, selection);
    visit(632, sortColumn);
    visit(633, sortDirection);
    visit(634, topItem);
    visit(630, ownerDrawItemHeight);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(columns, adopt);
    VWidget.adoptEach(editors, adopt);
    VWidget.adoptEach(items, adopt);
    VWidget.adoptEach(selection, adopt);
    sortColumn = VWidget.adoptOne(sortColumn, adopt);
    topItem = VWidget.adoptOne(topItem, adopt);
  }
}
