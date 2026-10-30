import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VTouchSource implements WireReadable, WireWritable, WireVisitable {
  VTouchSource() : this.empty();
  VTouchSource.empty();

  @override
  bool readWire(int key, WidgetWireReader r) => false;

  @override
  void visitWire(void Function(int key, Object? value) visit) {}

  @override
  void writeWire(WidgetWireWriter w) {}
}
