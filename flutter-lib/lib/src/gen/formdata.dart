import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VFormData implements WireReadable, WireWritable, WireVisitable {
  VFormData() : this.empty();
  VFormData.empty();

  @override
  bool readWire(int key, WidgetWireReader r) => false;

  @override
  void visitWire(void Function(int key, Object? value) visit) {}

  @override
  void writeWire(WidgetWireWriter w) {}
}
