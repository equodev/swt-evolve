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
      case 620: // columnOrder
        columnOrder = r.ints();
        return true;
      case 621: // columns
        columns = r.refs<VTreeColumn>();
        return true;
      case 622: // editors
        editors = r.refs<VTreeEditor>();
        return true;
      case 623: // headerBackground
        headerBackground = r.color();
        return true;
      case 624: // headerForeground
        headerForeground = r.color();
        return true;
      case 625: // headerVisible
        headerVisible = r.boolean();
        return true;
      case 626: // items
        items = r.refs<VTreeItem>();
        return true;
      case 627: // linesVisible
        linesVisible = r.boolean();
        return true;
      case 629: // selection
        selection = r.refs<VTreeItem>();
        return true;
      case 630: // sortColumn
        sortColumn = r.ref<VTreeColumn>();
        return true;
      case 631: // sortDirection
        sortDirection = r.int32();
        return true;
      case 632: // topItem
        topItem = r.ref<VTreeItem>();
        return true;
      case 628: // ownerDrawItemHeight
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
        w.key(620);
        w.ints(v_);
      }
    }
    {
      final v_ = columns;
      if (v_ != null) {
        w.key(621);
        w.refs(v_);
      }
    }
    {
      final v_ = editors;
      if (v_ != null) {
        w.key(622);
        w.refs(v_);
      }
    }
    {
      final v_ = headerBackground;
      if (v_ != null) {
        w.key(623);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerForeground;
      if (v_ != null) {
        w.key(624);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerVisible;
      if (v_ != null) {
        w.keyBoolean(625, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(626);
        w.refs(v_);
      }
    }
    {
      final v_ = linesVisible;
      if (v_ != null) {
        w.keyBoolean(627, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(629);
        w.refs(v_);
      }
    }
    {
      final v_ = sortColumn;
      if (v_ != null) {
        w.keyRef(630, v_.id);
      }
    }
    {
      final v_ = sortDirection;
      if (v_ != null) {
        w.keyInt32(631, v_);
      }
    }
    {
      final v_ = topItem;
      if (v_ != null) {
        w.keyRef(632, v_.id);
      }
    }
    {
      final v_ = ownerDrawItemHeight;
      if (v_ != null) {
        w.keyInt32(628, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(620, columnOrder);
    visit(621, columns);
    visit(622, editors);
    visit(623, headerBackground);
    visit(624, headerForeground);
    visit(625, headerVisible);
    visit(626, items);
    visit(627, linesVisible);
    visit(629, selection);
    visit(630, sortColumn);
    visit(631, sortDirection);
    visit(632, topItem);
    visit(628, ownerDrawItemHeight);
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
