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
      case 589: // background
        background = r.color();
        return true;
      case 590: // borderColor
        borderColor = r.color();
        return true;
      case 591: // borderStyle
        borderStyle = r.int32();
        return true;
      case 592: // font
        font = r.object(VFont.new);
        return true;
      case 593: // foreground
        foreground = r.color();
        return true;
      case 594: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 595: // rise
        rise = r.int32();
        return true;
      case 596: // strikeout
        strikeout = r.boolean();
        return true;
      case 597: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 598: // underline
        underline = r.boolean();
        return true;
      case 599: // underlineColor
        underlineColor = r.color();
        return true;
      case 600: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(589, background);
    visit(590, borderColor);
    visit(591, borderStyle);
    visit(592, font);
    visit(593, foreground);
    visit(594, metrics);
    visit(595, rise);
    visit(596, strikeout);
    visit(597, strikeoutColor);
    visit(598, underline);
    visit(599, underlineColor);
    visit(600, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(591, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(595, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(596, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(598, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(600, v_);
      }
    }
  }
}
