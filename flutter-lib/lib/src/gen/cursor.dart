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
      case 153: // cursorStyle
        cursorStyle = r.int32();
        return true;
      case 154: // hotspotX
        hotspotX = r.int32();
        return true;
      case 155: // hotspotY
        hotspotY = r.int32();
        return true;
      case 156: // image
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
        w.keyInt32(153, v_);
      }
    }
    {
      final v_ = hotspotX;
      if (v_ != null) {
        w.keyInt32(154, v_);
      }
    }
    {
      final v_ = hotspotY;
      if (v_ != null) {
        w.keyInt32(155, v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(156, v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(153, cursorStyle);
    visit(154, hotspotX);
    visit(155, hotspotY);
    visit(156, image);
  }
}
