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
      case 155: // cursorStyle
        cursorStyle = r.int32();
        return true;
      case 156: // hotspotX
        hotspotX = r.int32();
        return true;
      case 157: // hotspotY
        hotspotY = r.int32();
        return true;
      case 158: // image
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
        w.keyInt32(155, v_);
      }
    }
    {
      final v_ = hotspotX;
      if (v_ != null) {
        w.keyInt32(156, v_);
      }
    }
    {
      final v_ = hotspotY;
      if (v_ != null) {
        w.keyInt32(157, v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(158, v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(155, cursorStyle);
    visit(156, hotspotX);
    visit(157, hotspotY);
    visit(158, image);
  }
}
