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
      case 429: // height
        height = r.int32();
        return true;
      case 430: // width
        width = r.int32();
        return true;
      case 431: // x
        x = r.int32();
        return true;
      case 432: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(429, height);
    visit(430, width);
    visit(431, x);
    visit(432, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = height;
      w.keyInt32(429, v_);
    }
    {
      final v_ = width;
      w.keyInt32(430, v_);
    }
    {
      final v_ = x;
      w.keyInt32(431, v_);
    }
    {
      final v_ = y;
      w.keyInt32(432, v_);
    }
  }
}
