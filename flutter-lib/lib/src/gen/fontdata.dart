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
      case 234: // height
        height = r.int32();
        return true;
      case 235: // name
        name = r.string() ?? '';
        return true;
      case 236: // style
        style = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = height;
      w.keyInt32(234, v_);
    }
    {
      final v_ = name;
      w.key(235);
      w.string(v_);
    }
    {
      final v_ = style;
      w.keyInt32(236, v_);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(234, height);
    visit(235, name);
    visit(236, style);
  }
}
