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
      case 164: // cursorStyle
        cursorStyle = r.int32();
        return true;
      case 165: // hotspotX
        hotspotX = r.int32();
        return true;
      case 166: // hotspotY
        hotspotY = r.int32();
        return true;
      case 167: // image
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
        w.keyInt32(164, v_);
      }
    }
    {
      final v_ = hotspotX;
      if (v_ != null) {
        w.keyInt32(165, v_);
      }
    }
    {
      final v_ = hotspotY;
      if (v_ != null) {
        w.keyInt32(166, v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(167, v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(164, cursorStyle);
    visit(165, hotspotX);
    visit(166, hotspotY);
    visit(167, image);
  }
}
