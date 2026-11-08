import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/point.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/tooltip_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class ToolTipSwt<V extends VToolTip> extends WidgetSwt<V> {
  const ToolTipSwt({super.key, required super.value});

  @override
  State createState() => ToolTipImpl<ToolTipSwt<VToolTip>, VToolTip>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
  }
}

class VToolTip extends VWidget {
  VToolTip() : this.empty();
  VToolTip.empty() {
    swt = "ToolTip";
  }

  VPoint? location;
  String? message;
  String? text;
  bool? visible;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VToolTip) {
      location = other.location;
      message = other.message;
      text = other.text;
      visible = other.visible;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 612: // location
        location = r.point();
        return true;
      case 613: // message
        message = r.string();
        return true;
      case 614: // text
        text = r.string();
        return true;
      case 615: // visible
        visible = r.boolean();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = location;
      if (v_ != null) {
        w.key(612);
        w.pointValue(v_);
      }
    }
    {
      final v_ = message;
      if (v_ != null) {
        w.key(613);
        w.string(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(614);
        w.string(v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null) {
        w.keyBoolean(615, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(612, location);
    visit(613, message);
    visit(614, text);
    visit(615, visible);
  }

  @override
  String? get semanticsText => text;
}
