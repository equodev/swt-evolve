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
    sendEvent(val, "Control/Move", payload);
  }

  void sendControlResize(V val, VEvent? payload) {
    sendEvent(val, "Control/Resize", payload);
  }

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
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
      case 212: // alignment
        alignment = r.int32();
        return true;
      case 1380: // moveable
        moveable = r.boolean();
        return true;
      case 213: // resizable
        resizable = r.boolean();
        return true;
      case 214: // width
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
      if (v_ != null && w.wants(212)) {
        w.key(212);
        w.int32(v_);
      }
    }
    {
      final v_ = moveable;
      if (v_ != null && w.wants(1380)) {
        w.key(1380);
        w.boolean(v_);
      }
    }
    {
      final v_ = resizable;
      if (v_ != null && w.wants(213)) {
        w.key(213);
        w.boolean(v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null && w.wants(214)) {
        w.key(214);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(212, alignment);
    visit(1380, moveable);
    visit(213, resizable);
    visit(214, width);
  }
}
