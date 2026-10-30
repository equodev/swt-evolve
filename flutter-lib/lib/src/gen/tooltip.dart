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
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
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
      case 164: // location
        location = r.point();
        return true;
      case 165: // message
        message = r.string();
        return true;
      case 166: // text
        text = r.string();
        return true;
      case 167: // visible
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
      if (v_ != null && w.wants(164)) {
        w.key(164);
        w.pointValue(v_);
      }
    }
    {
      final v_ = message;
      if (v_ != null && w.wants(165)) {
        w.key(165);
        w.string(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null && w.wants(166)) {
        w.key(166);
        w.string(v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null && w.wants(167)) {
        w.key(167);
        w.boolean(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(164, location);
    visit(165, message);
    visit(166, text);
    visit(167, visible);
  }

  @override
  String? get semanticsText => text;
}
