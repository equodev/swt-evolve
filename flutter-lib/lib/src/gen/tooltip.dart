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
      case 623: // location
        location = r.point();
        return true;
      case 624: // message
        message = r.string();
        return true;
      case 625: // text
        text = r.string();
        return true;
      case 626: // visible
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
        w.key(623);
        w.pointValue(v_);
      }
    }
    {
      final v_ = message;
      if (v_ != null) {
        w.key(624);
        w.string(v_);
      }
    }
    {
      final v_ = text;
      if (v_ != null) {
        w.key(625);
        w.string(v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null) {
        w.keyBoolean(626, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(623, location);
    visit(624, message);
    visit(625, text);
    visit(626, visible);
  }

  @override
  String? get semanticsText => text;
}
