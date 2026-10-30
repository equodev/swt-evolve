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
import '../impl/slider_evolve.dart';
import 'event.dart';
import 'widgets.dart';

class SliderSwt<V extends VSlider> extends ControlSwt<V> {
  const SliderSwt({super.key, required super.value});

  @override
  State createState() => SliderImpl<SliderSwt<VSlider>, VSlider>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

class VSlider extends VControl {
  VSlider() : this.empty();
  VSlider.empty() {
    swt = "Slider";
  }

  int? increment;
  int? maximum;
  int? minimum;
  int? pageIncrement;
  int? selection;
  int? thumb;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VSlider) {
      increment = other.increment;
      maximum = other.maximum;
      minimum = other.minimum;
      pageIncrement = other.pageIncrement;
      selection = other.selection;
      thumb = other.thumb;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 50: // increment
        increment = r.int32();
        return true;
      case 51: // maximum
        maximum = r.int32();
        return true;
      case 52: // minimum
        minimum = r.int32();
        return true;
      case 53: // pageIncrement
        pageIncrement = r.int32();
        return true;
      case 54: // selection
        selection = r.int32();
        return true;
      case 55: // thumb
        thumb = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = increment;
      if (v_ != null && w.wants(50)) {
        w.key(50);
        w.int32(v_);
      }
    }
    {
      final v_ = maximum;
      if (v_ != null && w.wants(51)) {
        w.key(51);
        w.int32(v_);
      }
    }
    {
      final v_ = minimum;
      if (v_ != null && w.wants(52)) {
        w.key(52);
        w.int32(v_);
      }
    }
    {
      final v_ = pageIncrement;
      if (v_ != null && w.wants(53)) {
        w.key(53);
        w.int32(v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null && w.wants(54)) {
        w.key(54);
        w.int32(v_);
      }
    }
    {
      final v_ = thumb;
      if (v_ != null && w.wants(55)) {
        w.key(55);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(50, increment);
    visit(51, maximum);
    visit(52, minimum);
    visit(53, pageIncrement);
    visit(54, selection);
    visit(55, thumb);
  }
}
