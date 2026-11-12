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
      case 403: // color1
        color1 = r.color();
        return true;
      case 404: // color2
        color2 = r.color();
        return true;
      case 405: // endX
        endX = r.float32();
        return true;
      case 406: // endY
        endY = r.float32();
        return true;
      case 407: // image
        image = r.image();
        return true;
      case 408: // startX
        startX = r.float32();
        return true;
      case 409: // startY
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
        w.key(403);
        w.colorValue(v_);
      }
    }
    {
      final v_ = color2;
      if (v_ != null) {
        w.key(404);
        w.colorValue(v_);
      }
    }
    {
      final v_ = endX;
      if (v_ != null) {
        w.key(405);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = endY;
      if (v_ != null) {
        w.key(406);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(407, v_.id);
      }
    }
    {
      final v_ = startX;
      if (v_ != null) {
        w.key(408);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = startY;
      if (v_ != null) {
        w.key(409);
        w.float32(v_.toDouble());
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(403, color1);
    visit(404, color2);
    visit(405, endX);
    visit(406, endY);
    visit(407, image);
    visit(408, startX);
    visit(409, startY);
  }
}
