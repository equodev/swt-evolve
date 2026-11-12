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
      case 598: // background
        background = r.color();
        return true;
      case 599: // borderColor
        borderColor = r.color();
        return true;
      case 600: // borderStyle
        borderStyle = r.int32();
        return true;
      case 601: // font
        font = r.object(VFont.new);
        return true;
      case 602: // foreground
        foreground = r.color();
        return true;
      case 603: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 604: // rise
        rise = r.int32();
        return true;
      case 605: // strikeout
        strikeout = r.boolean();
        return true;
      case 606: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 607: // underline
        underline = r.boolean();
        return true;
      case 608: // underlineColor
        underlineColor = r.color();
        return true;
      case 609: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(598, background);
    visit(599, borderColor);
    visit(600, borderStyle);
    visit(601, font);
    visit(602, foreground);
    visit(603, metrics);
    visit(604, rise);
    visit(605, strikeout);
    visit(606, strikeoutColor);
    visit(607, underline);
    visit(608, underlineColor);
    visit(609, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(600, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(604, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(605, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(607, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(609, v_);
      }
    }
  }
}
