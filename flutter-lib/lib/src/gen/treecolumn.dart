import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/image.dart';
import '../gen/item.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/treecolumn_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class TreeColumnSwt<V extends VTreeColumn> extends ItemSwt<V> {
  const TreeColumnSwt({super.key, required super.value});

  @override
  State createState() =>
      TreeColumnImpl<TreeColumnSwt<VTreeColumn>, VTreeColumn>();

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

class VTreeColumn extends VItem {
  VTreeColumn() : this.empty();
  VTreeColumn.empty() {
    swt = "TreeColumn";
  }

  int? alignment;
  bool? moveable;
  bool? resizable;
  int? width;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTreeColumn) {
      alignment = other.alignment;
      moveable = other.moveable;
      resizable = other.resizable;
      width = other.width;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 176: // alignment
        alignment = r.int32();
        return true;
      case 177: // moveable
        moveable = r.boolean();
        return true;
      case 1379: // resizable
        resizable = r.boolean();
        return true;
      case 178: // width
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
      if (v_ != null && w.wants(176)) {
        w.key(176);
        w.int32(v_);
      }
    }
    {
      final v_ = moveable;
      if (v_ != null && w.wants(177)) {
        w.key(177);
        w.boolean(v_);
      }
    }
    {
      final v_ = resizable;
      if (v_ != null && w.wants(1379)) {
        w.key(1379);
        w.boolean(v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null && w.wants(178)) {
        w.key(178);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(176, alignment);
    visit(177, moveable);
    visit(1379, resizable);
    visit(178, width);
  }
}
