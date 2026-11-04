import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/image.dart';
import '../gen/widgets.dart';

class VPattern implements WireReadable, WireWritable, WireVisitable {
  VPattern() : this.empty();
  VPattern.empty();

  VColor? color1;
  VColor? color2;
  double? endX;
  double? endY;
  VImage? image;
  double? startX;
  double? startY;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 398: // color1
        color1 = r.color();
        return true;
      case 399: // color2
        color2 = r.color();
        return true;
      case 400: // endX
        endX = r.float32();
        return true;
      case 401: // endY
        endY = r.float32();
        return true;
      case 402: // image
        image = r.image();
        return true;
      case 403: // startX
        startX = r.float32();
        return true;
      case 404: // startY
        startY = r.float32();
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = color1;
      if (v_ != null) {
        w.key(398);
        w.colorValue(v_);
      }
    }
    {
      final v_ = color2;
      if (v_ != null) {
        w.key(399);
        w.colorValue(v_);
      }
    }
    {
      final v_ = endX;
      if (v_ != null) {
        w.key(400);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = endY;
      if (v_ != null) {
        w.key(401);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(402, v_.id);
      }
    }
    {
      final v_ = startX;
      if (v_ != null) {
        w.key(403);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = startY;
      if (v_ != null) {
        w.key(404);
        w.float32(v_.toDouble());
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(398, color1);
    visit(399, color2);
    visit(400, endX);
    visit(401, endY);
    visit(402, image);
    visit(403, startX);
    visit(404, startY);
  }
}
