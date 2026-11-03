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
      case 154: // cursorStyle
        cursorStyle = r.int32();
        return true;
      case 155: // hotspotX
        hotspotX = r.int32();
        return true;
      case 156: // hotspotY
        hotspotY = r.int32();
        return true;
      case 157: // image
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
        w.keyInt32(154, v_);
      }
    }
    {
      final v_ = hotspotX;
      if (v_ != null) {
        w.keyInt32(155, v_);
      }
    }
    {
      final v_ = hotspotY;
      if (v_ != null) {
        w.keyInt32(156, v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(157, v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(154, cursorStyle);
    visit(155, hotspotX);
    visit(156, hotspotY);
    visit(157, image);
  }
}
