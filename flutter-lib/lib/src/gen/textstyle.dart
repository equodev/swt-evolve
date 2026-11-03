import '../comm/widget_wire.dart';
import '../gen/color.dart';
import '../gen/font.dart';
import '../gen/glyphmetrics.dart';
import '../gen/widgets.dart';

class VTextStyle implements WireReadable, WireWritable, WireVisitable {
  VTextStyle() : this.empty();
  VTextStyle.empty();

  VColor? background;
  VColor? borderColor;
  int? borderStyle;
  VFont? font;
  VColor? foreground;
  VGlyphMetrics? metrics;
  int? rise;
  bool? strikeout;
  VColor? strikeoutColor;
  bool? underline;
  VColor? underlineColor;
  int? underlineStyle;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 587: // background
        background = r.color();
        return true;
      case 588: // borderColor
        borderColor = r.color();
        return true;
      case 589: // borderStyle
        borderStyle = r.int32();
        return true;
      case 590: // font
        font = r.object(VFont.new);
        return true;
      case 591: // foreground
        foreground = r.color();
        return true;
      case 592: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 593: // rise
        rise = r.int32();
        return true;
      case 594: // strikeout
        strikeout = r.boolean();
        return true;
      case 595: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 596: // underline
        underline = r.boolean();
        return true;
      case 597: // underlineColor
        underlineColor = r.color();
        return true;
      case 598: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(587, background);
    visit(588, borderColor);
    visit(589, borderStyle);
    visit(590, font);
    visit(591, foreground);
    visit(592, metrics);
    visit(593, rise);
    visit(594, strikeout);
    visit(595, strikeoutColor);
    visit(596, underline);
    visit(597, underlineColor);
    visit(598, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(589, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(593, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(594, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(596, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(598, v_);
      }
    }
  }
}
