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
      case 76: // a
        alpha = r.int32();
        return true;
      case 77: // b
        blue = r.int32();
        return true;
      case 78: // g
        green = r.int32();
        return true;
      case 79: // r
        red = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = alpha;
      w.keyInt32(76, v_);
    }
    {
      final v_ = blue;
      w.keyInt32(77, v_);
    }
    {
      final v_ = green;
      w.keyInt32(78, v_);
    }
    {
      final v_ = red;
      w.keyInt32(79, v_);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(76, alpha);
    visit(77, blue);
    visit(78, green);
    visit(79, red);
  }
}
