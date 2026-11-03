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
    sendEvent(val, 86, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 88, payload); // Selection/Selection
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
      case 433: // enabled
        enabled = r.boolean();
        return true;
      case 434: // increment
        increment = r.int32();
        return true;
      case 435: // maximum
        maximum = r.int32();
        return true;
      case 436: // minimum
        minimum = r.int32();
        return true;
      case 437: // selection
        selection = r.int32();
        return true;
      case 438: // thumb
        thumb = r.int32();
        return true;
      case 439: // visible
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
      if (v_ != null) {
        w.keyBoolean(433, v_);
      }
    }
    {
      final v_ = increment;
      if (v_ != null) {
        w.keyInt32(434, v_);
      }
    }
    {
      final v_ = maximum;
      if (v_ != null) {
        w.keyInt32(435, v_);
      }
    }
    {
      final v_ = minimum;
      if (v_ != null) {
        w.keyInt32(436, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyInt32(437, v_);
      }
    }
    {
      final v_ = thumb;
      if (v_ != null) {
        w.keyInt32(438, v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null) {
        w.keyBoolean(439, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(433, enabled);
    visit(434, increment);
    visit(435, maximum);
    visit(436, minimum);
    visit(437, selection);
    visit(438, thumb);
    visit(439, visible);
  }

  @override
  bool? get semanticsEnabled => enabled;
}
