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
      case 410: // x
        x = r.int32();
        return true;
      case 411: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(410, x);
    visit(411, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = x;
      w.keyInt32(410, v_);
    }
    {
      final v_ = y;
      w.keyInt32(411, v_);
    }
  }
}
