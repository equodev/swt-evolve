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
      case 232: // height
        height = r.int32();
        return true;
      case 233: // name
        name = r.string() ?? '';
        return true;
      case 234: // style
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
      w.keyInt32(232, v_);
    }
    {
      final v_ = name;
      w.key(233);
      w.string(v_);
    }
    {
      final v_ = style;
      w.keyInt32(234, v_);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(232, height);
    visit(233, name);
    visit(234, style);
  }
}
