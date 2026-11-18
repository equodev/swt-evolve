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
  bool? compactRows;

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
      compactRows = other.compactRows;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 5952: // columnOrder
        columnOrder = r.ints();
        return true;
      case 5953: // columns
        columns = r.refs<VTreeColumn>(columns);
        return true;
      case 5954: // editors
        editors = r.refs<VTreeEditor>(editors);
        return true;
      case 5955: // headerBackground
        headerBackground = r.color();
        return true;
      case 5956: // headerForeground
        headerForeground = r.color();
        return true;
      case 5957: // headerVisible
        headerVisible = r.boolean();
        return true;
      case 5958: // items
        items = r.items<VTreeItem>(WireType.treeItem, items);
        return true;
      case 5959: // linesVisible
        linesVisible = r.boolean();
        return true;
      case 5961: // selection
        selection = r.refs<VTreeItem>(selection);
        return true;
      case 5962: // sortColumn
        sortColumn = r.ref<VTreeColumn>();
        return true;
      case 5963: // sortDirection
        sortDirection = r.int32();
        return true;
      case 5964: // topItem
        topItem = r.ref<VTreeItem>();
        return true;
      case 5960: // ownerDrawItemHeight
        ownerDrawItemHeight = r.int32();
        return true;
      case 5965: // compactRows
        compactRows = r.boolean();
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
        w.key(5952);
        w.ints(v_);
      }
    }
    {
      final v_ = columns;
      if (v_ != null) {
        w.key(5953);
        w.refs(v_);
      }
    }
    {
      final v_ = editors;
      if (v_ != null) {
        w.key(5954);
        w.refs(v_);
      }
    }
    {
      final v_ = headerBackground;
      if (v_ != null) {
        w.key(5955);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerForeground;
      if (v_ != null) {
        w.key(5956);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerVisible;
      if (v_ != null) {
        w.keyBoolean(5957, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(5958);
        w.items(v_);
      }
    }
    {
      final v_ = linesVisible;
      if (v_ != null) {
        w.keyBoolean(5959, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(5961);
        w.refs(v_);
      }
    }
    {
      final v_ = sortColumn;
      if (v_ != null) {
        w.keyRef(5962, v_.id);
      }
    }
    {
      final v_ = sortDirection;
      if (v_ != null) {
        w.keyInt32(5963, v_);
      }
    }
    {
      final v_ = topItem;
      if (v_ != null) {
        w.keyRef(5964, v_.id);
      }
    }
    {
      final v_ = ownerDrawItemHeight;
      if (v_ != null) {
        w.keyInt32(5960, v_);
      }
    }
    {
      final v_ = compactRows;
      if (v_ != null) {
        w.keyBoolean(5965, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(5952, columnOrder);
    visit(5953, columns);
    visit(5954, editors);
    visit(5955, headerBackground);
    visit(5956, headerForeground);
    visit(5957, headerVisible);
    visit(5958, items);
    visit(5959, linesVisible);
    visit(5961, selection);
    visit(5962, sortColumn);
    visit(5963, sortDirection);
    visit(5964, topItem);
    visit(5960, ownerDrawItemHeight);
    visit(5965, compactRows);
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
