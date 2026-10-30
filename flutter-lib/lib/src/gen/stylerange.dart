import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/glyphmetrics.dart';
import '../gen/textstyle.dart';
import '../gen/widgets.dart';

class VStyleRange extends VTextStyle {
  VStyleRange() : this.empty();
  VStyleRange.empty() : super.empty();

  int fontStyle = 0;
  int length = 0;
  int start = 0;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 554: // fontStyle
        fontStyle = r.int32();
        return true;
      case 555: // length
        length = r.int32();
        return true;
      case 556: // start
        start = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(554, fontStyle);
    visit(555, length);
    visit(556, start);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = fontStyle;
      w.key(554);
      w.int32(v_);
    }
    {
      final v_ = length;
      w.key(555);
      w.int32(v_);
    }
    {
      final v_ = start;
      w.key(556);
      w.int32(v_);
    }
  }
}
