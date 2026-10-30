import 'package:flutter/widgets.dart';
import '../comm/comm.dart';
import '../comm/widget_wire.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/scrollbar_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class ScrollBarSwt<V extends VScrollBar> extends WidgetSwt<V> {
  const ScrollBarSwt({super.key, required super.value});

  @override
  State createState() => ScrollBarImpl<ScrollBarSwt<VScrollBar>, VScrollBar>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

class VScrollBar extends VWidget {
  VScrollBar() : this.empty();
  VScrollBar.empty() {
    swt = "ScrollBar";
  }

  bool? enabled;
  int? increment;
  int? maximum;
  int? minimum;
  int? selection;
  int? thumb;
  bool? visible;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VScrollBar) {
      enabled = other.enabled;
      increment = other.increment;
      maximum = other.maximum;
      minimum = other.minimum;
      selection = other.selection;
      thumb = other.thumb;
      visible = other.visible;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 324: // enabled
        enabled = r.boolean();
        return true;
      case 325: // increment
        increment = r.int32();
        return true;
      case 326: // maximum
        maximum = r.int32();
        return true;
      case 327: // minimum
        minimum = r.int32();
        return true;
      case 328: // selection
        selection = r.int32();
        return true;
      case 329: // thumb
        thumb = r.int32();
        return true;
      case 330: // visible
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
      final v_ = enabled;
      if (v_ != null && w.wants(324)) {
        w.key(324);
        w.boolean(v_);
      }
    }
    {
      final v_ = increment;
      if (v_ != null && w.wants(325)) {
        w.key(325);
        w.int32(v_);
      }
    }
    {
      final v_ = maximum;
      if (v_ != null && w.wants(326)) {
        w.key(326);
        w.int32(v_);
      }
    }
    {
      final v_ = minimum;
      if (v_ != null && w.wants(327)) {
        w.key(327);
        w.int32(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(328)) {
        w.key(328);
        w.int32(v_);
      }
    }
    {
      final v_ = thumb;
      if (v_ != null && w.wants(329)) {
        w.key(329);
        w.int32(v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null && w.wants(330)) {
        w.key(330);
        w.boolean(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(324, enabled);
    visit(325, increment);
    visit(326, maximum);
    visit(327, minimum);
    visit(328, selection);
    visit(329, thumb);
    visit(330, visible);
  }

  @override
  bool? get semanticsEnabled => enabled;
}
