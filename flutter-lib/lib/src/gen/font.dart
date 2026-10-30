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
      case 424: // fontData
        fontData = r
            .list(() => r.object(VFontData.new))
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
      if (v_ != null && w.wants(424)) {
        w.key(424);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(424, fontData);
  }
}
