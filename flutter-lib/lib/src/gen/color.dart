import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VColor implements WireReadable, WireWritable, WireVisitable {
  VColor() : this.empty();
  VColor.empty();

  /// A colour packed as `0xAARRGGBB`, as a resource's change sends it.
  factory VColor.fromArgb(int argb) => VColor()
    ..alpha = (argb >> 24) & 0xFF
    ..red = (argb >> 16) & 0xFF
    ..green = (argb >> 8) & 0xFF
    ..blue = argb & 0xFF;

  int alpha = 0;
  int blue = 0;
  int green = 0;
  int red = 0;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 45: // a
        alpha = r.int32();
        return true;
      case 46: // b
        blue = r.int32();
        return true;
      case 47: // g
        green = r.int32();
        return true;
      case 48: // r
        red = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    if (w.wants(45)) {
      final v_ = alpha;
      w.key(45);
      w.int32(v_);
    }
    if (w.wants(46)) {
      final v_ = blue;
      w.key(46);
      w.int32(v_);
    }
    if (w.wants(47)) {
      final v_ = green;
      w.key(47);
      w.int32(v_);
    }
    if (w.wants(48)) {
      final v_ = red;
      w.key(48);
      w.int32(v_);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(45, alpha);
    visit(46, blue);
    visit(47, green);
    visit(48, red);
  }
}
