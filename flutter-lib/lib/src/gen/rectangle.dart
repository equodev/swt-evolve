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
      case 427: // height
        height = r.int32();
        return true;
      case 428: // width
        width = r.int32();
        return true;
      case 429: // x
        x = r.int32();
        return true;
      case 430: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(427, height);
    visit(428, width);
    visit(429, x);
    visit(430, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = height;
      w.keyInt32(427, v_);
    }
    {
      final v_ = width;
      w.keyInt32(428, v_);
    }
    {
      final v_ = x;
      w.keyInt32(429, v_);
    }
    {
      final v_ = y;
      w.keyInt32(430, v_);
    }
  }
}
