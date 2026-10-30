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
      case 404: // height
        height = r.int32();
        return true;
      case 405: // width
        width = r.int32();
        return true;
      case 406: // x
        x = r.int32();
        return true;
      case 407: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(404, height);
    visit(405, width);
    visit(406, x);
    visit(407, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = height;
      w.key(404);
      w.int32(v_);
    }
    {
      final v_ = width;
      w.key(405);
      w.int32(v_);
    }
    {
      final v_ = x;
      w.key(406);
      w.int32(v_);
    }
    {
      final v_ = y;
      w.key(407);
      w.int32(v_);
    }
  }
}
