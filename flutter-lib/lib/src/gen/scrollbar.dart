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
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
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
  int? pageIncrement;
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
      pageIncrement = other.pageIncrement;
      selection = other.selection;
      thumb = other.thumb;
      visible = other.visible;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 440: // enabled
        enabled = r.boolean();
        return true;
      case 441: // increment
        increment = r.int32();
        return true;
      case 442: // maximum
        maximum = r.int32();
        return true;
      case 443: // minimum
        minimum = r.int32();
        return true;
      case 444: // pageIncrement
        pageIncrement = r.int32();
        return true;
      case 445: // selection
        selection = r.int32();
        return true;
      case 446: // thumb
        thumb = r.int32();
        return true;
      case 447: // visible
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
        w.keyBoolean(440, v_);
      }
    }
    {
      final v_ = increment;
      if (v_ != null) {
        w.keyInt32(441, v_);
      }
    }
    {
      final v_ = maximum;
      if (v_ != null) {
        w.keyInt32(442, v_);
      }
    }
    {
      final v_ = minimum;
      if (v_ != null) {
        w.keyInt32(443, v_);
      }
    }
    {
      final v_ = pageIncrement;
      if (v_ != null) {
        w.keyInt32(444, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyInt32(445, v_);
      }
    }
    {
      final v_ = thumb;
      if (v_ != null) {
        w.keyInt32(446, v_);
      }
    }
    {
      final v_ = visible;
      if (v_ != null) {
        w.keyBoolean(447, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(440, enabled);
    visit(441, increment);
    visit(442, maximum);
    visit(443, minimum);
    visit(444, pageIncrement);
    visit(445, selection);
    visit(446, thumb);
    visit(447, visible);
  }

  @override
  bool? get semanticsEnabled => enabled;
}
