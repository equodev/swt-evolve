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
      case 1419: // image
        image = r.image();
        return true;
      case 1420: // visible
        visible = r.boolean();
        return true;
      case 365: // bounds
        bounds = r.rect();
        return true;
      case 1418: // focused
        focused = r.boolean();
        return true;
      case 1417: // blinkRate
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
      if (v_ != null && w.wants(1419)) {
        w.key(1419);
        w.ref(v_.id);
      }
    }
    {
      final v_ = visible;
      if (v_ != null && w.wants(1420)) {
        w.key(1420);
        w.boolean(v_);
      }
    }
    {
      final v_ = bounds;
      if (v_ != null && w.wants(365)) {
        w.key(365);
        w.rectValue(v_);
      }
    }
    {
      final v_ = focused;
      if (v_ != null && w.wants(1418)) {
        w.key(1418);
        w.boolean(v_);
      }
    }
    {
      final v_ = blinkRate;
      if (v_ != null && w.wants(1417)) {
        w.key(1417);
        w.int32(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(1419, image);
    visit(1420, visible);
    visit(365, bounds);
    visit(1418, focused);
    visit(1417, blinkRate);
  }
}
