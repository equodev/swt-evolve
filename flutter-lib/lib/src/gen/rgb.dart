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
      case 109: // blue
        blue = r.int32();
        return true;
      case 110: // green
        green = r.int32();
        return true;
      case 111: // red
        red = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(109, blue);
    visit(110, green);
    visit(111, red);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = blue;
      w.key(109);
      w.int32(v_);
    }
    {
      final v_ = green;
      w.key(110);
      w.int32(v_);
    }
    {
      final v_ = red;
      w.key(111);
      w.int32(v_);
    }
  }
}
