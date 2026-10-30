import '../comm/widget_wire.dart';
import '../gen/fontdata.dart';
import '../gen/widgets.dart';

class VFont implements WireReadable, WireWritable, WireVisitable {
  VFont() : this.empty();
  VFont.empty();

  List<VFontData>? fontData;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 231: // fontData
        fontData = r
            .list((r) => r.object(VFontData.new))
            ?.whereType<VFontData>()
            .toList();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = fontData;
      if (v_ != null) {
        w.key(231);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(231, fontData);
  }
}
