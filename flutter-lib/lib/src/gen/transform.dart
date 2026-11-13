import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VTransform implements WireReadable, WireWritable, WireVisitable {
  VTransform() : this.empty();
  VTransform.empty();

  List<double>? elements;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 636: // elements
        elements = r.floats();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = elements;
      if (v_ != null) {
        w.key(636);
        w.floats(v_);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(636, elements);
  }
}
