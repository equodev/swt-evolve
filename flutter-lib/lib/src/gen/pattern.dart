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
      case 394: // color1
        color1 = r.color();
        return true;
      case 395: // color2
        color2 = r.color();
        return true;
      case 396: // endX
        endX = r.float32();
        return true;
      case 397: // endY
        endY = r.float32();
        return true;
      case 398: // image
        image = r.image();
        return true;
      case 399: // startX
        startX = r.float32();
        return true;
      case 400: // startY
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
        w.key(394);
        w.colorValue(v_);
      }
    }
    {
      final v_ = color2;
      if (v_ != null) {
        w.key(395);
        w.colorValue(v_);
      }
    }
    {
      final v_ = endX;
      if (v_ != null) {
        w.key(396);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = endY;
      if (v_ != null) {
        w.key(397);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(398, v_.id);
      }
    }
    {
      final v_ = startX;
      if (v_ != null) {
        w.key(399);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = startY;
      if (v_ != null) {
        w.key(400);
        w.float32(v_.toDouble());
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(394, color1);
    visit(395, color2);
    visit(396, endX);
    visit(397, endY);
    visit(398, image);
    visit(399, startX);
    visit(400, startY);
  }
}
