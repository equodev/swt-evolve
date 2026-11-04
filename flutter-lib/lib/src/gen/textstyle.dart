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
      case 588: // background
        background = r.color();
        return true;
      case 589: // borderColor
        borderColor = r.color();
        return true;
      case 590: // borderStyle
        borderStyle = r.int32();
        return true;
      case 591: // font
        font = r.object(VFont.new);
        return true;
      case 592: // foreground
        foreground = r.color();
        return true;
      case 593: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 594: // rise
        rise = r.int32();
        return true;
      case 595: // strikeout
        strikeout = r.boolean();
        return true;
      case 596: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 597: // underline
        underline = r.boolean();
        return true;
      case 598: // underlineColor
        underlineColor = r.color();
        return true;
      case 599: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(588, background);
    visit(589, borderColor);
    visit(590, borderStyle);
    visit(591, font);
    visit(592, foreground);
    visit(593, metrics);
    visit(594, rise);
    visit(595, strikeout);
    visit(596, strikeoutColor);
    visit(597, underline);
    visit(598, underlineColor);
    visit(599, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(590, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(594, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(595, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(597, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(599, v_);
      }
    }
  }
}
