import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VFontData implements WireReadable, WireWritable, WireVisitable {
  VFontData() : this.empty();
  VFontData.empty();

  int height = 0;
  String name = "";
  int style = 0;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 409: // height
        height = r.int32();
        return true;
      case 410: // name
        name = r.string() ?? '';
        return true;
      case 411: // style
        style = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    if (w.wants(409)) {
      final v_ = height;
      w.key(409);
      w.int32(v_);
    }
    if (w.wants(410)) {
      final v_ = name;
      w.key(410);
      w.string(v_);
    }
    if (w.wants(411)) {
      final v_ = style;
      w.key(411);
      w.int32(v_);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(409, height);
    visit(410, name);
    visit(411, style);
  }
}
