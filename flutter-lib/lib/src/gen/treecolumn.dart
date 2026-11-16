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

class VTreeColumn extends VItem {
  VTreeColumn() : this.empty();
  VTreeColumn.empty() {
    swt = "TreeColumn";
  }

  int? alignment;
  bool? moveable;
  bool? resizable;
  String? toolTipText;
  int? width;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VTreeColumn) {
      alignment = other.alignment;
      moveable = other.moveable;
      resizable = other.resizable;
      toolTipText = other.toolTipText;
      width = other.width;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 651: // alignment
        alignment = r.int32();
        return true;
      case 652: // moveable
        moveable = r.boolean();
        return true;
      case 653: // resizable
        resizable = r.boolean();
        return true;
      case 654: // toolTipText
        toolTipText = r.string();
        return true;
      case 655: // width
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
        w.keyInt32(651, v_);
      }
    }
    {
      final v_ = moveable;
      if (v_ != null) {
        w.keyBoolean(652, v_);
      }
    }
    {
      final v_ = resizable;
      if (v_ != null) {
        w.keyBoolean(653, v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null) {
        w.key(654);
        w.string(v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(655, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(651, alignment);
    visit(652, moveable);
    visit(653, resizable);
    visit(654, toolTipText);
    visit(655, width);
  }

  @override
  String? get semanticsTooltip => toolTipText;
}
