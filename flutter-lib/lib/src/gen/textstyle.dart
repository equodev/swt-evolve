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
      case 585: // background
        background = r.color();
        return true;
      case 586: // borderColor
        borderColor = r.color();
        return true;
      case 587: // borderStyle
        borderStyle = r.int32();
        return true;
      case 588: // font
        font = r.object(VFont.new);
        return true;
      case 589: // foreground
        foreground = r.color();
        return true;
      case 590: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 591: // rise
        rise = r.int32();
        return true;
      case 592: // strikeout
        strikeout = r.boolean();
        return true;
      case 593: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 594: // underline
        underline = r.boolean();
        return true;
      case 595: // underlineColor
        underlineColor = r.color();
        return true;
      case 596: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(585, background);
    visit(586, borderColor);
    visit(587, borderStyle);
    visit(588, font);
    visit(589, foreground);
    visit(590, metrics);
    visit(591, rise);
    visit(592, strikeout);
    visit(593, strikeoutColor);
    visit(594, underline);
    visit(595, underlineColor);
    visit(596, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(587, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(591, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(592, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(594, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(596, v_);
      }
    }
  }
}
