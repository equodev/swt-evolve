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
      case 73: // image
        image = r.image();
        return true;
      case 74: // visible
        visible = r.boolean();
        return true;
      case 71: // bounds
        bounds = r.rect();
        return true;
      case 72: // focused
        focused = r.boolean();
        return true;
      case 70: // blinkRate
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
        w.keyRef(73, v_.id);
      }
    }
    {
      final v_ = visible;
      if (v_ != null) {
        w.keyBoolean(74, v_);
      }
    }
    {
      final v_ = bounds;
      if (v_ != null) {
        w.key(71);
        w.rectValue(v_);
      }
    }
    {
      final v_ = focused;
      if (v_ != null) {
        w.keyBoolean(72, v_);
      }
    }
    {
      final v_ = blinkRate;
      if (v_ != null) {
        w.keyInt32(70, v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(73, image);
    visit(74, visible);
    visit(71, bounds);
    visit(72, focused);
    visit(70, blinkRate);
  }
}
