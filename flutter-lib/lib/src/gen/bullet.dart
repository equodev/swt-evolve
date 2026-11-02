import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VBullet implements WireReadable, WireWritable, WireVisitable {
  VBullet() : this.empty();
  VBullet.empty();

  @override
  bool readWire(int key, WidgetWireReader r) => false;

  @override
  void writeWire(WidgetWireWriter w) {}

  @override
  void visitWire(void Function(int key, Object? value) visit) {}
}
