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
      case 547: // columnOrder
        columnOrder = r.ints();
        return true;
      case 548: // columns
        columns = r.refs<VTableColumn>();
        return true;
      case 549: // editors
        editors = r.refs<VTableEditor>();
        return true;
      case 550: // headerBackground
        headerBackground = r.color();
        return true;
      case 551: // headerForeground
        headerForeground = r.color();
        return true;
      case 552: // headerVisible
        headerVisible = r.boolean();
        return true;
      case 554: // items
        items = r.refs<VTableItem>();
        return true;
      case 555: // linesVisible
        linesVisible = r.boolean();
        return true;
      case 557: // selection
        selection = r.ints();
        return true;
      case 558: // topIndex
        topIndex = r.int32();
        return true;
      case 553: // itemCount
        itemCount = r.int32();
        return true;
      case 556: // ownerDrawItemHeight
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
        w.key(547);
        w.ints(v_);
      }
    }
    {
      final v_ = columns;
      if (v_ != null) {
        w.key(548);
        w.refs(v_);
      }
    }
    {
      final v_ = editors;
      if (v_ != null) {
        w.key(549);
        w.refs(v_);
      }
    }
    {
      final v_ = headerBackground;
      if (v_ != null) {
        w.key(550);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerForeground;
      if (v_ != null) {
        w.key(551);
        w.colorValue(v_);
      }
    }
    {
      final v_ = headerVisible;
      if (v_ != null) {
        w.keyBoolean(552, v_);
      }
    }
    {
      final v_ = items;
      if (v_ != null) {
        w.key(554);
        w.refs(v_);
      }
    }
    {
      final v_ = linesVisible;
      if (v_ != null) {
        w.keyBoolean(555, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.key(557);
        w.ints(v_);
      }
    }
    {
      final v_ = topIndex;
      if (v_ != null) {
        w.keyInt32(558, v_);
      }
    }
    {
      final v_ = itemCount;
      if (v_ != null) {
        w.keyInt32(553, v_);
      }
    }
    {
      final v_ = ownerDrawItemHeight;
      if (v_ != null) {
        w.keyInt32(556, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(547, columnOrder);
    visit(548, columns);
    visit(549, editors);
    visit(550, headerBackground);
    visit(551, headerForeground);
    visit(552, headerVisible);
    visit(554, items);
    visit(555, linesVisible);
    visit(557, selection);
    visit(558, topIndex);
    visit(553, itemCount);
    visit(556, ownerDrawItemHeight);
  }

  @override
  void adoptChildren(VWidget Function(VWidget) adopt) {
    super.adoptChildren(adopt);
    VWidget.adoptEach(columns, adopt);
    VWidget.adoptEach(editors, adopt);
    VWidget.adoptEach(items, adopt);
  }
}
