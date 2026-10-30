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
      case 570: // color1
        color1 = r.color();
        return true;
      case 571: // color2
        color2 = r.color();
        return true;
      case 572: // endX
        endX = r.float32();
        return true;
      case 573: // endY
        endY = r.float32();
        return true;
      case 574: // image
        image = r.image();
        return true;
      case 575: // startX
        startX = r.float32();
        return true;
      case 576: // startY
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
      if (v_ != null && w.wants(570)) {
        w.key(570);
        w.colorValue(v_);
      }
    }
    {
      final v_ = color2;
      if (v_ != null && w.wants(571)) {
        w.key(571);
        w.colorValue(v_);
      }
    }
    {
      final v_ = endX;
      if (v_ != null && w.wants(572)) {
        w.key(572);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = endY;
      if (v_ != null && w.wants(573)) {
        w.key(573);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = image;
      if (v_ != null && w.wants(574)) {
        w.key(574);
        w.ref(v_.id);
      }
    }
    {
      final v_ = startX;
      if (v_ != null && w.wants(575)) {
        w.key(575);
        w.float32(v_.toDouble());
      }
    }
    {
      final v_ = startY;
      if (v_ != null && w.wants(576)) {
        w.key(576);
        w.float32(v_.toDouble());
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(570, color1);
    visit(571, color2);
    visit(572, endX);
    visit(573, endY);
    visit(574, image);
    visit(575, startX);
    visit(576, startY);
  }
}
