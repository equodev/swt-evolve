import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VFormAttachment implements WireReadable, WireWritable, WireVisitable {
  VFormAttachment() : this.empty();
  VFormAttachment.empty();

  @override
  bool readWire(int key, WidgetWireReader r) => false;

  @override
  void visitWire(void Function(int key, Object? value) visit) {}

  @override
  void writeWire(WidgetWireWriter w) {}
}
