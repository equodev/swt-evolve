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
      case 420: // height
        height = r.int32();
        return true;
      case 421: // width
        width = r.int32();
        return true;
      case 422: // x
        x = r.int32();
        return true;
      case 423: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(420, height);
    visit(421, width);
    visit(422, x);
    visit(423, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = height;
      w.keyInt32(420, v_);
    }
    {
      final v_ = width;
      w.keyInt32(421, v_);
    }
    {
      final v_ = x;
      w.keyInt32(422, v_);
    }
    {
      final v_ = y;
      w.keyInt32(423, v_);
    }
  }
}
