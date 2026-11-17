import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VRectangle implements WireReadable, WireWritable, WireVisitable {
  VRectangle() : this.empty();
  VRectangle.empty();

  int height = 0;
  int width = 0;
  int x = 0;
  int y = 0;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 4096: // height
        height = r.int32();
        return true;
      case 4097: // width
        width = r.int32();
        return true;
      case 4098: // x
        x = r.int32();
        return true;
      case 4099: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(4096, height);
    visit(4097, width);
    visit(4098, x);
    visit(4099, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = height;
      w.keyInt32(4096, v_);
    }
    {
      final v_ = width;
      w.keyInt32(4097, v_);
    }
    {
      final v_ = x;
      w.keyInt32(4098, v_);
    }
    {
      final v_ = y;
      w.keyInt32(4099, v_);
    }
  }
}
