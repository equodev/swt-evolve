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
      case 584: // background
        background = r.color();
        return true;
      case 585: // borderColor
        borderColor = r.color();
        return true;
      case 586: // borderStyle
        borderStyle = r.int32();
        return true;
      case 587: // font
        font = r.object(VFont.new);
        return true;
      case 588: // foreground
        foreground = r.color();
        return true;
      case 589: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 590: // rise
        rise = r.int32();
        return true;
      case 591: // strikeout
        strikeout = r.boolean();
        return true;
      case 592: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 593: // underline
        underline = r.boolean();
        return true;
      case 594: // underlineColor
        underlineColor = r.color();
        return true;
      case 595: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(584, background);
    visit(585, borderColor);
    visit(586, borderStyle);
    visit(587, font);
    visit(588, foreground);
    visit(589, metrics);
    visit(590, rise);
    visit(591, strikeout);
    visit(592, strikeoutColor);
    visit(593, underline);
    visit(594, underlineColor);
    visit(595, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(586, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(590, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(591, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(593, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(595, v_);
      }
    }
  }
}
