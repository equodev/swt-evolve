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
      case 402: // x
        x = r.int32();
        return true;
      case 403: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(402, x);
    visit(403, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = x;
      w.keyInt32(402, v_);
    }
    {
      final v_ = y;
      w.keyInt32(403, v_);
    }
  }
}
