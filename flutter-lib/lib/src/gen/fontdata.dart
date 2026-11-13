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
      case 242: // height
        height = r.int32();
        return true;
      case 243: // name
        name = r.string() ?? '';
        return true;
      case 244: // style
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
      w.keyInt32(242, v_);
    }
    {
      final v_ = name;
      w.key(243);
      w.string(v_);
    }
    {
      final v_ = style;
      w.keyInt32(244, v_);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(242, height);
    visit(243, name);
    visit(244, style);
  }
}
