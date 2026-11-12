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
      case 239: // height
        height = r.int32();
        return true;
      case 240: // name
        name = r.string() ?? '';
        return true;
      case 241: // style
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
      w.keyInt32(239, v_);
    }
    {
      final v_ = name;
      w.key(240);
      w.string(v_);
    }
    {
      final v_ = style;
      w.keyInt32(241, v_);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(239, height);
    visit(240, name);
    visit(241, style);
  }
}
