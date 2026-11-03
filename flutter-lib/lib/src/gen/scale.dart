import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/scale_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class ScaleSwt<V extends VScale> extends ControlSwt<V> {
  const ScaleSwt({super.key, required super.value});

  @override
  State createState() => ScaleImpl<ScaleSwt<VScale>, VScale>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
  }
}

class VScale extends VControl {
  VScale() : this.empty();
  VScale.empty() {
    swt = "Scale";
  }

  int? maximum;
  int? minimum;
  int? selection;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VScale) {
      maximum = other.maximum;
      minimum = other.minimum;
      selection = other.selection;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 431: // maximum
        maximum = r.int32();
        return true;
      case 432: // minimum
        minimum = r.int32();
        return true;
      case 433: // selection
        selection = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = maximum;
      if (v_ != null) {
        w.keyInt32(431, v_);
      }
    }
    {
      final v_ = minimum;
      if (v_ != null) {
        w.keyInt32(432, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyInt32(433, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(431, maximum);
    visit(432, minimum);
    visit(433, selection);
  }
}
