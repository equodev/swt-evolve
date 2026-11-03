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
      case 586: // background
        background = r.color();
        return true;
      case 587: // borderColor
        borderColor = r.color();
        return true;
      case 588: // borderStyle
        borderStyle = r.int32();
        return true;
      case 589: // font
        font = r.object(VFont.new);
        return true;
      case 590: // foreground
        foreground = r.color();
        return true;
      case 591: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 592: // rise
        rise = r.int32();
        return true;
      case 593: // strikeout
        strikeout = r.boolean();
        return true;
      case 594: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 595: // underline
        underline = r.boolean();
        return true;
      case 596: // underlineColor
        underlineColor = r.color();
        return true;
      case 597: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(586, background);
    visit(587, borderColor);
    visit(588, borderStyle);
    visit(589, font);
    visit(590, foreground);
    visit(591, metrics);
    visit(592, rise);
    visit(593, strikeout);
    visit(594, strikeoutColor);
    visit(595, underline);
    visit(596, underlineColor);
    visit(597, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(588, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(592, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(593, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(595, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(597, v_);
      }
    }
  }
}
