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
      case 209: // x
        x = r.int32();
        return true;
      case 210: // y
        y = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(209, x);
    visit(210, y);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = x;
      w.key(209);
      w.int32(v_);
    }
    {
      final v_ = y;
      w.key(210);
      w.int32(v_);
    }
  }
}
