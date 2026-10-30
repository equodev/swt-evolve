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
      case 468: // fontStyle
        fontStyle = r.int32();
        return true;
      case 469: // length
        length = r.int32();
        return true;
      case 470: // start
        start = r.int32();
        return true;
      default:
        return super.readWire(key, r);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(468, fontStyle);
    visit(469, length);
    visit(470, start);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = fontStyle;
      w.keyInt32(468, v_);
    }
    {
      final v_ = length;
      w.keyInt32(469, v_);
    }
    {
      final v_ = start;
      w.keyInt32(470, v_);
    }
  }
}
