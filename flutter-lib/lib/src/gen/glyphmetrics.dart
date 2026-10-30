import '../comm/widget_wire.dart';
import '../gen/widgets.dart';

class VGlyphMetrics implements WireReadable, WireWritable, WireVisitable {
  VGlyphMetrics() : this.empty();
  VGlyphMetrics.empty();

  int ascent = 0;
  int descent = 0;
  int width = 0;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 523: // ascent
        ascent = r.int32();
        return true;
      case 524: // descent
        descent = r.int32();
        return true;
      case 525: // width
        width = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(523, ascent);
    visit(524, descent);
    visit(525, width);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = ascent;
      w.key(523);
      w.int32(v_);
    }
    {
      final v_ = descent;
      w.key(524);
      w.int32(v_);
    }
    {
      final v_ = width;
      w.key(525);
      w.int32(v_);
    }
  }
}
