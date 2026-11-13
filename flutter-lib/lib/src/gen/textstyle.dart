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
      case 601: // background
        background = r.color();
        return true;
      case 602: // borderColor
        borderColor = r.color();
        return true;
      case 603: // borderStyle
        borderStyle = r.int32();
        return true;
      case 604: // font
        font = r.object(VFont.new);
        return true;
      case 605: // foreground
        foreground = r.color();
        return true;
      case 606: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 607: // rise
        rise = r.int32();
        return true;
      case 608: // strikeout
        strikeout = r.boolean();
        return true;
      case 609: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 610: // underline
        underline = r.boolean();
        return true;
      case 611: // underlineColor
        underlineColor = r.color();
        return true;
      case 612: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(601, background);
    visit(602, borderColor);
    visit(603, borderStyle);
    visit(604, font);
    visit(605, foreground);
    visit(606, metrics);
    visit(607, rise);
    visit(608, strikeout);
    visit(609, strikeoutColor);
    visit(610, underline);
    visit(611, underlineColor);
    visit(612, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(603, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(607, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(608, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(610, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(612, v_);
      }
    }
  }
}
