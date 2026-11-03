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
      case 416: // blue
        blue = r.int32();
        return true;
      case 417: // green
        green = r.int32();
        return true;
      case 418: // red
        red = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(416, blue);
    visit(417, green);
    visit(418, red);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = blue;
      w.keyInt32(416, v_);
    }
    {
      final v_ = green;
      w.keyInt32(417, v_);
    }
    {
      final v_ = red;
      w.keyInt32(418, v_);
    }
  }
}
