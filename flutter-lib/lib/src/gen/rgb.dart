import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VRGB implements WireReadable, WireWritable, WireVisitable {
  VRGB() : this.empty();
  VRGB.empty();

  int blue = 0;
  int green = 0;
  int red = 0;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 418: // blue
        blue = r.int32();
        return true;
      case 419: // green
        green = r.int32();
        return true;
      case 420: // red
        red = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(418, blue);
    visit(419, green);
    visit(420, red);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = blue;
      w.keyInt32(418, v_);
    }
    {
      final v_ = green;
      w.keyInt32(419, v_);
    }
    {
      final v_ = red;
      w.keyInt32(420, v_);
    }
  }
}
