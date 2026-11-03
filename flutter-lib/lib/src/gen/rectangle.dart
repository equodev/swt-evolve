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
      case 419: // height
        height = r.int32();
        return true;
      case 420: // width
        width = r.int32();
        return true;
      case 421: // x
        x = r.int32();
        return true;
      case 422: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(419, height);
    visit(420, width);
    visit(421, x);
    visit(422, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = height;
      w.keyInt32(419, v_);
    }
    {
      final v_ = width;
      w.keyInt32(420, v_);
    }
    {
      final v_ = x;
      w.keyInt32(421, v_);
    }
    {
      final v_ = y;
      w.keyInt32(422, v_);
    }
  }
}
