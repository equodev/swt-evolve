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
      case 406: // color1
        color1 = r.color();
        return true;
      case 407: // color2
        color2 = r.color();
        return true;
      case 408: // endX
        endX = r.float32();
        return true;
      case 409: // endY
        endY = r.float32();
        return true;
      case 410: // image
        image = r.image();
        return true;
      case 411: // startX
        startX = r.float32();
        return true;
      case 412: // startY
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
        w.key(406);
        w.colorValue(v_);
      }
    }
    {
      final v_ = color2;
      if (v_ != null) {
        w.key(407);
        w.colorValue(v_);
      }
    }
    {
      final v_ = endX;
      if (v_ != null) {
        w.key(408);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = endY;
      if (v_ != null) {
        w.key(409);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(410, v_.id);
      }
    }
    {
      final v_ = startX;
      if (v_ != null) {
        w.key(411);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = startY;
      if (v_ != null) {
        w.key(412);
        w.float32(v_.toDouble());
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(406, color1);
    visit(407, color2);
    visit(408, endX);
    visit(409, endY);
    visit(410, image);
    visit(411, startX);
    visit(412, startY);
  }
}
