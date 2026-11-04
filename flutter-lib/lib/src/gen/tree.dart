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
    sendEvent(val, 72, payload); // Modify/Modify
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }

  void sendSetDataSetData(V val, VEvent? payload) {
    sendEvent(val, 96, payload); // SetData/SetData
  }

  void sendTreeCollapse(V val, VEvent? payload) {
    sendEvent(val, 112, payload); // Tree/Collapse
  }

  void sendTreeExpand(V val, VEvent? payload) {
    sendEvent(val, 113, payload); // Tree/Expand
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
      case 624: // columnOrder
        columnOrder = r.ints();
        return true;
      case 625: // columns
        columns = r.refs<VTreeColumn>();
        return true;
      case 626: // editors
        editors = r.refs<VTreeEditor>();
        return true;
      case 627: // headerBackground
        headerBackground = r.color();
        return true;
      case 628: // headerForeground
        headerForeground = r.color();
        return true;
      case 629: // headerVisible
        headerVisible = r.boolean();
        return true;
      case 630: // items
        items = r.refs<VTreeItem>();
        return true;
      case 631: // linesVisible
        linesVisible = r.boolean();
        return true;
      case 633: // selection
        selection = r.refs<VTreeItem>();
        return true;
      case 634: // sortColumn
        sortColumn = r.ref<VTreeColumn>();
        return true;
      case 635: // sortDirection
        sortDirection = r.int32();
        return true;
      case 636: // topItem
        topItem = r.ref<VTreeItem>();
        return true;
      case 632: // ownerDrawItemHeight
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
        w.key(624);
        w.ints(v_);
      }
    }
    {
      final v_ = columns;
      if (v_ != null) {
        w.key(625);
        w.refs(v_);
      }
    }
    {
      final v_ = editors;
      if (v_ != null) {
        w.key(626);
        w.refs(v_);
      }
    }
    {
      final v_ = headerBackground;
      if (v_ != null) {
        w.key(627);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerForeground;
      if (v_ != null) {
        w.key(628);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerVisible;
      if (v_ != null) {
        w.keyBoolean(629, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(630);
        w.refs(v_);
      }
    }
    {
      final v_ = linesVisible;
      if (v_ != null) {
        w.keyBoolean(631, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(633);
        w.refs(v_);
      }
    }
    {
      final v_ = sortColumn;
      if (v_ != null) {
        w.keyRef(634, v_.id);
      }
    }
    {
      final v_ = sortDirection;
      if (v_ != null) {
        w.keyInt32(635, v_);
      }
    }
    {
      final v_ = topItem;
      if (v_ != null) {
        w.keyRef(636, v_.id);
      }
    }
    {
      final v_ = ownerDrawItemHeight;
      if (v_ != null) {
        w.keyInt32(632, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(624, columnOrder);
    visit(625, columns);
    visit(626, editors);
    visit(627, headerBackground);
    visit(628, headerForeground);
    visit(629, headerVisible);
    visit(630, items);
    visit(631, linesVisible);
    visit(633, selection);
    visit(634, sortColumn);
    visit(635, sortDirection);
    visit(636, topItem);
    visit(632, ownerDrawItemHeight);
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
