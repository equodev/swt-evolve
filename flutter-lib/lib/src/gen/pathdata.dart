import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VPathData implements WireReadable, WireWritable, WireVisitable {
  VPathData() : this.empty();
  VPathData.empty();

  List<double>? points;
  List<int>? types;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 396: // points
        points = r.floats();
        return true;
      case 397: // types
        types = r.bytes();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = points;
      if (v_ != null) {
        w.key(396);
        w.floats(v_);
      }
    }
    {
      final v_ = types;
      if (v_ != null) {
        w.key(397);
        w.bytes(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(396, points);
    visit(397, types);
  }
}
