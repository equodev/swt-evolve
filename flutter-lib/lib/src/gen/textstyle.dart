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
      case 429: // background
        background = r.color();
        return true;
      case 430: // borderColor
        borderColor = r.color();
        return true;
      case 431: // borderStyle
        borderStyle = r.int32();
        return true;
      case 432: // font
        font = r.object(VFont.new);
        return true;
      case 433: // foreground
        foreground = r.color();
        return true;
      case 434: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 435: // rise
        rise = r.int32();
        return true;
      case 436: // strikeout
        strikeout = r.boolean();
        return true;
      case 437: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 438: // underline
        underline = r.boolean();
        return true;
      case 439: // underlineColor
        underlineColor = r.color();
        return true;
      case 440: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(429, background);
    visit(430, borderColor);
    visit(431, borderStyle);
    visit(432, font);
    visit(433, foreground);
    visit(434, metrics);
    visit(435, rise);
    visit(436, strikeout);
    visit(437, strikeoutColor);
    visit(438, underline);
    visit(439, underlineColor);
    visit(440, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.key(431);
        w.int32(v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.key(435);
        w.int32(v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.key(436);
        w.boolean(v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.key(438);
        w.boolean(v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.key(440);
        w.int32(v_);
      }
    }
  }
}
