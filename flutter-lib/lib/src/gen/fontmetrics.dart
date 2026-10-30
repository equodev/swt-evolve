import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VFontMetrics implements WireReadable, WireWritable, WireVisitable {
  VFontMetrics() : this.empty();
  VFontMetrics.empty();

  @override
  bool readWire(int key, WidgetWireReader r) => false;

  @override
  void writeWire(WidgetWireWriter w) {}

  @override
  void visitWire(void Function(int key, Object? value) visit) {}
}
