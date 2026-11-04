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
      case 397: // points
        points = r.floats();
        return true;
      case 398: // types
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
        w.key(397);
        w.floats(v_);
      }
    }
    {
      final v_ = types;
      if (v_ != null) {
        w.key(398);
        w.bytes(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(397, points);
    visit(398, types);
  }
}
