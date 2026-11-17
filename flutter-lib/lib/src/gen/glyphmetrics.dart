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
      case 2560: // ascent
        ascent = r.int32();
        return true;
      case 2561: // descent
        descent = r.int32();
        return true;
      case 2562: // width
        width = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(2560, ascent);
    visit(2561, descent);
    visit(2562, width);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = ascent;
      w.keyInt32(2560, v_);
    }
    {
      final v_ = descent;
      w.keyInt32(2561, v_);
    }
    {
      final v_ = width;
      w.keyInt32(2562, v_);
    }
  }
}
