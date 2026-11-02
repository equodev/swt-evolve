import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VBorderData implements WireReadable, WireWritable, WireVisitable {
  VBorderData() : this.empty();
  VBorderData.empty();

  @override
  bool readWire(int key, WidgetWireReader r) => false;

  @override
  void visitWire(void Function(int key, Object? value) visit) {}

  @override
  void writeWire(WidgetWireWriter w) {}
}
