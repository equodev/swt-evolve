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
    sendEvent(val, 93, payload); // Selection/DefaultSelection
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, 95, payload); // Selection/Selection
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
      case 467: // increment
        increment = r.int32();
        return true;
      case 468: // maximum
        maximum = r.int32();
        return true;
      case 469: // minimum
        minimum = r.int32();
        return true;
      case 470: // pageIncrement
        pageIncrement = r.int32();
        return true;
      case 471: // selection
        selection = r.int32();
        return true;
      case 472: // thumb
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
      if (v_ != null) {
        w.keyInt32(467, v_);
      }
    }
    {
      final v_ = maximum;
      if (v_ != null) {
        w.keyInt32(468, v_);
      }
    }
    {
      final v_ = minimum;
      if (v_ != null) {
        w.keyInt32(469, v_);
      }
    }
    {
      final v_ = pageIncrement;
      if (v_ != null) {
        w.keyInt32(470, v_);
      }
    }
    {
      final v_ = selection;
      if (v_ != null) {
        w.keyInt32(471, v_);
      }
    }
    {
      final v_ = thumb;
      if (v_ != null) {
        w.keyInt32(472, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(467, increment);
    visit(468, maximum);
    visit(469, minimum);
    visit(470, pageIncrement);
    visit(471, selection);
    visit(472, thumb);
  }
}
