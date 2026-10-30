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
      case 536: // cursorStyle
        cursorStyle = r.int32();
        return true;
      case 537: // hotspotX
        hotspotX = r.int32();
        return true;
      case 538: // hotspotY
        hotspotY = r.int32();
        return true;
      case 539: // image
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
      if (v_ != null && w.wants(536)) {
        w.key(536);
        w.int32(v_);
      }
    }
    {
      final v_ = hotspotX;
      if (v_ != null && w.wants(537)) {
        w.key(537);
        w.int32(v_);
      }
    }
    {
      final v_ = hotspotY;
      if (v_ != null && w.wants(538)) {
        w.key(538);
        w.int32(v_);
      }
    }
    {
      final v_ = image;
      if (v_ != null && w.wants(539)) {
        w.key(539);
        w.ref(v_.id);
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(536, cursorStyle);
    visit(537, hotspotX);
    visit(538, hotspotY);
    visit(539, image);
  }
}
