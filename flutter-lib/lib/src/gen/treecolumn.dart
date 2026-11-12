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
      case 647: // alignment
        alignment = r.int32();
        return true;
      case 648: // moveable
        moveable = r.boolean();
        return true;
      case 649: // resizable
        resizable = r.boolean();
        return true;
      case 650: // toolTipText
        toolTipText = r.string();
        return true;
      case 651: // width
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
        w.keyInt32(647, v_);
      }
    }
    {
      final v_ = moveable;
      if (v_ != null) {
        w.keyBoolean(648, v_);
      }
    }
    {
      final v_ = resizable;
      if (v_ != null) {
        w.keyBoolean(649, v_);
      }
    }
    {
      final v_ = toolTipText;
      if (v_ != null) {
        w.key(650);
        w.string(v_);
      }
    }
    {
      final v_ = width;
      if (v_ != null) {
        w.keyInt32(651, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(647, alignment);
    visit(648, moveable);
    visit(649, resizable);
    visit(650, toolTipText);
    visit(651, width);
  }

  @override
  String? get semanticsTooltip => toolTipText;
}
