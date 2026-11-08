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
      case 590: // background
        background = r.color();
        return true;
      case 591: // borderColor
        borderColor = r.color();
        return true;
      case 592: // borderStyle
        borderStyle = r.int32();
        return true;
      case 593: // font
        font = r.object(VFont.new);
        return true;
      case 594: // foreground
        foreground = r.color();
        return true;
      case 595: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 596: // rise
        rise = r.int32();
        return true;
      case 597: // strikeout
        strikeout = r.boolean();
        return true;
      case 598: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 599: // underline
        underline = r.boolean();
        return true;
      case 600: // underlineColor
        underlineColor = r.color();
        return true;
      case 601: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(590, background);
    visit(591, borderColor);
    visit(592, borderStyle);
    visit(593, font);
    visit(594, foreground);
    visit(595, metrics);
    visit(596, rise);
    visit(597, strikeout);
    visit(598, strikeoutColor);
    visit(599, underline);
    visit(600, underlineColor);
    visit(601, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(592, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(596, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(597, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(599, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(601, v_);
      }
    }
  }
}
