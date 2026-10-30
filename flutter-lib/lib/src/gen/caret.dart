import 'package:flutter/widgets.dart';
import '../comm/widget_wire.dart';
import '../gen/image.dart';
import '../gen/rectangle.dart';
import '../gen/widget.dart';
import '../gen/widgets.dart';
import '../impl/caret_evolve.dart';
import 'widgets.dart';

class CaretSwt<V extends VCaret> extends WidgetSwt<V> {
  const CaretSwt({super.key, required super.value});

  @override
  State createState() => CaretImpl<CaretSwt<VCaret>, VCaret>();
}

class VCaret extends VWidget {
  VCaret() : this.empty();
  VCaret.empty() {
    swt = "Caret";
  }

  VImage? image;
  bool? visible;

  VRectangle? bounds;
  bool? focused;
  int? blinkRate;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VCaret) {
      image = other.image;
      visible = other.visible;
      bounds = other.bounds;
      focused = other.focused;
      blinkRate = other.blinkRate;
    }
  }

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 72: // image
        image = r.image();
        return true;
      case 73: // visible
        visible = r.boolean();
        return true;
      case 70: // bounds
        bounds = r.rect();
        return true;
      case 71: // focused
        focused = r.boolean();
        return true;
      case 69: // blinkRate
        blinkRate = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(72, v_.id);
      }
    }
    {
      final v_ = visible;
      if (v_ != null) {
        w.keyBoolean(73, v_);
      }
    }
    {
      final v_ = bounds;
      if (v_ != null) {
        w.key(70);
        w.rectValue(v_);
      }
    }
    {
      final v_ = focused;
      if (v_ != null) {
        w.keyBoolean(71, v_);
      }
    }
    {
      final v_ = blinkRate;
      if (v_ != null) {
        w.keyInt32(69, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(72, image);
    visit(73, visible);
    visit(70, bounds);
    visit(71, focused);
    visit(69, blinkRate);
  }
}
