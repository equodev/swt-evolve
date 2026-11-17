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
      case 3712: // color1
        color1 = r.color();
        return true;
      case 3713: // color2
        color2 = r.color();
        return true;
      case 3714: // endX
        endX = r.float32();
        return true;
      case 3715: // endY
        endY = r.float32();
        return true;
      case 3716: // image
        image = r.image();
        return true;
      case 3717: // startX
        startX = r.float32();
        return true;
      case 3718: // startY
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
        w.key(3712);
        w.colorValue(v_);
      }
    }
    {
      final v_ = color2;
      if (v_ != null) {
        w.key(3713);
        w.colorValue(v_);
      }
    }
    {
      final v_ = endX;
      if (v_ != null) {
        w.key(3714);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = endY;
      if (v_ != null) {
        w.key(3715);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = image;
      if (v_ != null) {
        w.keyRef(3716, v_.id);
      }
    }
    {
      final v_ = startX;
      if (v_ != null) {
        w.key(3717);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = startY;
      if (v_ != null) {
        w.key(3718);
        w.float32(v_.toDouble());
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(3712, color1);
    visit(3713, color2);
    visit(3714, endX);
    visit(3715, endY);
    visit(3716, image);
    visit(3717, startX);
    visit(3718, startY);
  }
}
