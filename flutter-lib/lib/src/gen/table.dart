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
import '../gen/tablecolumn.dart';
import '../gen/tableeditor.dart';
import '../gen/tableitem.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/table_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class TableSwt<V extends VTable> extends CompositeSwt<V> {
  const TableSwt({super.key, required super.value});

  @override
  State createState() => TableImpl<TableSwt<VTable>, VTable>();

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
}

class VTable extends VComposite {
  VTable() : this.empty();
  VTable.empty() {
    swt = "Table";
  }

  int? itemCount;
  List<int>? columnOrder;
  List<VTableColumn>? columns;
  List<VTableEditor>? editors;
  VColor? headerBackground;
  VColor? headerForeground;
  bool? headerVisible;
  List<VTableItem>? items;
  bool? linesVisible;
  List<int>? selection;
  int? topIndex;

  int? ownerDrawItemHeight;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTable) {
      columnOrder = other.columnOrder;
      columns = other.columns;
      editors = other.editors;
      headerBackground = other.headerBackground;
      headerForeground = other.headerForeground;
      headerVisible = other.headerVisible;
      items = other.items;
      linesVisible = other.linesVisible;
      selection = other.selection;
      topIndex = other.topIndex;
      itemCount = other.itemCount;
      ownerDrawItemHeight = other.ownerDrawItemHeight;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 1388: // columnOrder
        columnOrder = r.ints();
        return true;
      case 465: // columns
        columns = r.refs<VTableColumn>();
        return true;
      case 466: // editors
        editors = r.refs<VTableEditor>();
        return true;
      case 467: // headerBackground
        headerBackground = r.color();
        return true;
      case 468: // headerForeground
        headerForeground = r.color();
        return true;
      case 469: // headerVisible
        headerVisible = r.boolean();
        return true;
      case 471: // items
        items = r.refs<VTableItem>();
        return true;
      case 472: // linesVisible
        linesVisible = r.boolean();
        return true;
      case 473: // selection
        selection = r.ints();
        return true;
      case 1353: // topIndex
        topIndex = r.int32();
        return true;
      case 470: // itemCount
        itemCount = r.int32();
        return true;
      case 1423: // ownerDrawItemHeight
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
      if (v_ != null && w.wants(1388)) {
        w.key(1388);
        w.ints(v_);
      }
    }
    {
      final v_ = columns;
      if (v_ != null && w.wants(465)) {
        w.key(465);
        w.refs(v_);
      }
    }
    {
      final v_ = editors;
      if (v_ != null && w.wants(466)) {
        w.key(466);
        w.refs(v_);
      }
    }
    {
      final v_ = headerBackground;
      if (v_ != null && w.wants(467)) {
        w.key(467);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerForeground;
      if (v_ != null && w.wants(468)) {
        w.key(468);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerVisible;
      if (v_ != null && w.wants(469)) {
        w.key(469);
        w.boolean(v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null && w.wants(471)) {
        w.key(471);
        w.refs(v_);
      }
    }
    {
      final v_ = linesVisible;
      if (v_ != null && w.wants(472)) {
        w.key(472);
        w.boolean(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(473)) {
        w.key(473);
        w.ints(v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null && w.wants(1353)) {
        w.key(1353);
        w.int32(v_);
      }
    }
    {
      final v_ = itemCount;
      if (v_ != null && w.wants(470)) {
        w.key(470);
        w.int32(v_);
      }
    }
    {
      final v_ = ownerDrawItemHeight;
      if (v_ != null && w.wants(1423)) {
        w.key(1423);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(1388, columnOrder);
    visit(465, columns);
    visit(466, editors);
    visit(467, headerBackground);
    visit(468, headerForeground);
    visit(469, headerVisible);
    visit(471, items);
    visit(472, linesVisible);
    visit(473, selection);
    visit(1353, topIndex);
    visit(470, itemCount);
    visit(1423, ownerDrawItemHeight);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(columns, adopt);
    VWidget.adoptEach(editors, adopt);
    VWidget.adoptEach(items, adopt);
  }
}
