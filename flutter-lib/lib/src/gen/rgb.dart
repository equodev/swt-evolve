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
      case 423: // blue
        blue = r.int32();
        return true;
      case 424: // green
        green = r.int32();
        return true;
      case 425: // red
        red = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(423, blue);
    visit(424, green);
    visit(425, red);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = blue;
      w.keyInt32(423, v_);
    }
    {
      final v_ = green;
      w.keyInt32(424, v_);
    }
    {
      final v_ = red;
      w.keyInt32(425, v_);
    }
  }
}
