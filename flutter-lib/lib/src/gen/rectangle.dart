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
      case 422: // height
        height = r.int32();
        return true;
      case 423: // width
        width = r.int32();
        return true;
      case 424: // x
        x = r.int32();
        return true;
      case 425: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(422, height);
    visit(423, width);
    visit(424, x);
    visit(425, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = height;
      w.keyInt32(422, v_);
    }
    {
      final v_ = width;
      w.keyInt32(423, v_);
    }
    {
      final v_ = x;
      w.keyInt32(424, v_);
    }
    {
      final v_ = y;
      w.keyInt32(425, v_);
    }
  }
}
