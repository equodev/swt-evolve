import '../comm/widget_wire.dart';
import '../gen/image.dart';
import '../gen/widgets.dart';

class VCursor implements WireReadable, WireWritable, WireVisitable {
  VCursor() : this.empty();
  VCursor.empty();

  int? cursorStyle;
  int? hotspotX;
  int? hotspotY;
  VImage? image;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 161: // cursorStyle
        cursorStyle = r.int32();
        return true;
      case 162: // hotspotX
        hotspotX = r.int32();
        return true;
      case 163: // hotspotY
        hotspotY = r.int32();
        return true;
      case 164: // image
        image = r.image();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = cursorStyle;
      if (v_ != null) {
        w.keyInt32(161, v_);
      }
    }
    {
      final v_ = hotspotX;
      if (v_ != null) {
        w.keyInt32(162, v_);
      }
    }
    {
      final v_ = hotspotY;
      if (v_ != null) {
        w.keyInt32(163, v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(164, v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(161, cursorStyle);
    visit(162, hotspotX);
    visit(163, hotspotY);
    visit(164, image);
  }
}
