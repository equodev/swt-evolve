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
      case 634: // columnOrder
        columnOrder = r.ints();
        return true;
      case 635: // columns
        columns = r.refs<VTreeColumn>(columns);
        return true;
      case 636: // editors
        editors = r.refs<VTreeEditor>(editors);
        return true;
      case 637: // headerBackground
        headerBackground = r.color();
        return true;
      case 638: // headerForeground
        headerForeground = r.color();
        return true;
      case 639: // headerVisible
        headerVisible = r.boolean();
        return true;
      case 640: // items
        items = r.items<VTreeItem>(WireType.treeItem, items);
        return true;
      case 641: // linesVisible
        linesVisible = r.boolean();
        return true;
      case 643: // selection
        selection = r.refs<VTreeItem>(selection);
        return true;
      case 644: // sortColumn
        sortColumn = r.ref<VTreeColumn>();
        return true;
      case 645: // sortDirection
        sortDirection = r.int32();
        return true;
      case 646: // topItem
        topItem = r.ref<VTreeItem>();
        return true;
      case 642: // ownerDrawItemHeight
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
        w.key(634);
        w.ints(v_);
      }
    }
    {
      final v_ = columns;
      if (v_ != null) {
        w.key(635);
        w.refs(v_);
      }
    }
    {
      final v_ = editors;
      if (v_ != null) {
        w.key(636);
        w.refs(v_);
      }
    }
    {
      final v_ = headerBackground;
      if (v_ != null) {
        w.key(637);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerForeground;
      if (v_ != null) {
        w.key(638);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerVisible;
      if (v_ != null) {
        w.keyBoolean(639, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(640);
        w.items(v_);
      }
    }
    {
      final v_ = linesVisible;
      if (v_ != null) {
        w.keyBoolean(641, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(643);
        w.refs(v_);
      }
    }
    {
      final v_ = sortColumn;
      if (v_ != null) {
        w.keyRef(644, v_.id);
      }
    }
    {
      final v_ = sortDirection;
      if (v_ != null) {
        w.keyInt32(645, v_);
      }
    }
    {
      final v_ = topItem;
      if (v_ != null) {
        w.keyRef(646, v_.id);
      }
    }
    {
      final v_ = ownerDrawItemHeight;
      if (v_ != null) {
        w.keyInt32(642, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(634, columnOrder);
    visit(635, columns);
    visit(636, editors);
    visit(637, headerBackground);
    visit(638, headerForeground);
    visit(639, headerVisible);
    visit(640, items);
    visit(641, linesVisible);
    visit(643, selection);
    visit(644, sortColumn);
    visit(645, sortDirection);
    visit(646, topItem);
    visit(642, ownerDrawItemHeight);
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
