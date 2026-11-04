import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/tablecolumn_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class TableColumnSwt<V extends VTableColumn> extends ItemSwt<V> {
  const TableColumnSwt({super.key, required super.value});

  @override
  State createState() =>
      TableColumnImpl<TableColumnSwt<VTableColumn>, VTableColumn>();

  void sendControlMove(V val, VEvent? payload) {
    sendEvent(val, 20, payload); // Control/Move
  }

  void sendControlResize(V val, VEvent? payload) {
    sendEvent(val, 21, payload); // Control/Resize
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }
}

class VTableColumn extends VItem {
  VTableColumn() : this.empty();
  VTableColumn.empty() {
    swt = "TableColumn";
  }

  int? alignment;
  bool? moveable;
  bool? resizable;
  int? width;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTableColumn) {
      alignment = other.alignment;
      moveable = other.moveable;
      resizable = other.resizable;
      width = other.width;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 562: // alignment
        alignment = r.int32();
        return true;
      case 563: // moveable
        moveable = r.boolean();
        return true;
      case 564: // resizable
        resizable = r.boolean();
        return true;
      case 565: // width
        width = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = alignment;
      if (v_ != null) {
        w.keyInt32(562, v_);
      }
    }
    {
      final v_ = moveable;
      if (v_ != null) {
        w.keyBoolean(563, v_);
      }
    }
    {
      final v_ = resizable;
      if (v_ != null) {
        w.keyBoolean(564, v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(565, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(562, alignment);
    visit(563, moveable);
    visit(564, resizable);
    visit(565, width);
  }
}
