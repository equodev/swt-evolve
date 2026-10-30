import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VPoint implements WireReadable, WireWritable, WireVisitable {
  VPoint() : this.empty();
  VPoint.empty();

  int x = 0;
  int y = 0;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 401: // x
        x = r.int32();
        return true;
      case 402: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(401, x);
    visit(402, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = x;
      w.keyInt32(401, v_);
    }
    {
      final v_ = y;
      w.keyInt32(402, v_);
    }
  }
}
