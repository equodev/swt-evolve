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
      case 599: // background
        background = r.color();
        return true;
      case 600: // borderColor
        borderColor = r.color();
        return true;
      case 601: // borderStyle
        borderStyle = r.int32();
        return true;
      case 602: // font
        font = r.object(VFont.new);
        return true;
      case 603: // foreground
        foreground = r.color();
        return true;
      case 604: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 605: // rise
        rise = r.int32();
        return true;
      case 606: // strikeout
        strikeout = r.boolean();
        return true;
      case 607: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 608: // underline
        underline = r.boolean();
        return true;
      case 609: // underlineColor
        underlineColor = r.color();
        return true;
      case 610: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(599, background);
    visit(600, borderColor);
    visit(601, borderStyle);
    visit(602, font);
    visit(603, foreground);
    visit(604, metrics);
    visit(605, rise);
    visit(606, strikeout);
    visit(607, strikeoutColor);
    visit(608, underline);
    visit(609, underlineColor);
    visit(610, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(601, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(605, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(606, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(608, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(610, v_);
      }
    }
  }
}
