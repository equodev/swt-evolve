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
      case 1408: // cursorStyle
        cursorStyle = r.int32();
        return true;
      case 1409: // hotspotX
        hotspotX = r.int32();
        return true;
      case 1410: // hotspotY
        hotspotY = r.int32();
        return true;
      case 1411: // image
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
        w.keyInt32(1408, v_);
      }
    }
    {
      final v_ = hotspotX;
      if (v_ != null) {
        w.keyInt32(1409, v_);
      }
    }
    {
      final v_ = hotspotY;
      if (v_ != null) {
        w.keyInt32(1410, v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(1411, v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(1408, cursorStyle);
    visit(1409, hotspotX);
    visit(1410, hotspotY);
    visit(1411, image);
  }
}
