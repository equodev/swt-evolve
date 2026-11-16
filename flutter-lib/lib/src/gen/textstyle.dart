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
      case 602: // background
        background = r.color();
        return true;
      case 603: // borderColor
        borderColor = r.color();
        return true;
      case 604: // borderStyle
        borderStyle = r.int32();
        return true;
      case 605: // font
        font = r.object(VFont.new);
        return true;
      case 606: // foreground
        foreground = r.color();
        return true;
      case 607: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 608: // rise
        rise = r.int32();
        return true;
      case 609: // strikeout
        strikeout = r.boolean();
        return true;
      case 610: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 611: // underline
        underline = r.boolean();
        return true;
      case 612: // underlineColor
        underlineColor = r.color();
        return true;
      case 613: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(602, background);
    visit(603, borderColor);
    visit(604, borderStyle);
    visit(605, font);
    visit(606, foreground);
    visit(607, metrics);
    visit(608, rise);
    visit(609, strikeout);
    visit(610, strikeoutColor);
    visit(611, underline);
    visit(612, underlineColor);
    visit(613, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(604, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(608, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(609, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(611, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(613, v_);
      }
    }
  }
}
