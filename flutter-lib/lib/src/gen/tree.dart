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
    sendEvent(val, "Modify/Modify", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }

  void sendSetDataSetData(V val, VEvent? payload) {
    sendEvent(val, "SetData/SetData", payload);
  }

  void sendTreeCollapse(V val, VEvent? payload) {
    sendEvent(val, "Tree/Collapse", payload);
  }

  void sendTreeExpand(V val, VEvent? payload) {
    sendEvent(val, "Tree/Expand", payload);
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
      case 1383: // columnOrder
        columnOrder = r.ints();
        return true;
      case 454: // columns
        columns = r.refs<VTreeColumn>();
        return true;
      case 455: // editors
        editors = r.refs<VTreeEditor>();
        return true;
      case 456: // headerBackground
        headerBackground = r.color();
        return true;
      case 1384: // headerForeground
        headerForeground = r.color();
        return true;
      case 457: // headerVisible
        headerVisible = r.boolean();
        return true;
      case 458: // items
        items = r.refs<VTreeItem>();
        return true;
      case 459: // linesVisible
        linesVisible = r.boolean();
        return true;
      case 460: // selection
        selection = r.refs<VTreeItem>();
        return true;
      case 1385: // sortColumn
        sortColumn = r.ref<VTreeColumn>();
        return true;
      case 1386: // sortDirection
        sortDirection = r.int32();
        return true;
      case 1387: // topItem
        topItem = r.ref<VTreeItem>();
        return true;
      case 1421: // ownerDrawItemHeight
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
      if (v_ != null && w.wants(1383)) {
        w.key(1383);
        w.ints(v_);
      }
    }
    {
      final v_ = columns;
      if (v_ != null && w.wants(454)) {
        w.key(454);
        w.refs(v_);
      }
    }
    {
      final v_ = editors;
      if (v_ != null && w.wants(455)) {
        w.key(455);
        w.refs(v_);
      }
    }
    {
      final v_ = headerBackground;
      if (v_ != null && w.wants(456)) {
        w.key(456);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerForeground;
      if (v_ != null && w.wants(1384)) {
        w.key(1384);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerVisible;
      if (v_ != null && w.wants(457)) {
        w.key(457);
        w.boolean(v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null && w.wants(458)) {
        w.key(458);
        w.refs(v_);
      }
    }
    {
      final v_ = linesVisible;
      if (v_ != null && w.wants(459)) {
        w.key(459);
        w.boolean(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(460)) {
        w.key(460);
        w.refs(v_);
      }
    }
    {
      final v_ = sortColumn;
      if (v_ != null && w.wants(1385)) {
        w.key(1385);
        w.ref(v_.id);
      }
    }
    {
      final v_ = sortDirection;
      if (v_ != null && w.wants(1386)) {
        w.key(1386);
        w.int32(v_);
      }
    }
    {
      final v_ = topItem;
      if (v_ != null && w.wants(1387)) {
        w.key(1387);
        w.ref(v_.id);
      }
    }
    {
      final v_ = ownerDrawItemHeight;
      if (v_ != null && w.wants(1421)) {
        w.key(1421);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(1383, columnOrder);
    visit(454, columns);
    visit(455, editors);
    visit(456, headerBackground);
    visit(1384, headerForeground);
    visit(457, headerVisible);
    visit(458, items);
    visit(459, linesVisible);
    visit(460, selection);
    visit(1385, sortColumn);
    visit(1386, sortDirection);
    visit(1387, topItem);
    visit(1421, ownerDrawItemHeight);
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
