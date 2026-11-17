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
      case 5504: // background
        background = r.color();
        return true;
      case 5505: // borderColor
        borderColor = r.color();
        return true;
      case 5506: // borderStyle
        borderStyle = r.int32();
        return true;
      case 5507: // font
        font = r.object(VFont.new);
        return true;
      case 5508: // foreground
        foreground = r.color();
        return true;
      case 5509: // metrics
        metrics = r.object(VGlyphMetrics.new);
        return true;
      case 5510: // rise
        rise = r.int32();
        return true;
      case 5511: // strikeout
        strikeout = r.boolean();
        return true;
      case 5512: // strikeoutColor
        strikeoutColor = r.color();
        return true;
      case 5513: // underline
        underline = r.boolean();
        return true;
      case 5514: // underlineColor
        underlineColor = r.color();
        return true;
      case 5515: // underlineStyle
        underlineStyle = r.int32();
        return true;
      default:
        return false;
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(5504, background);
    visit(5505, borderColor);
    visit(5506, borderStyle);
    visit(5507, font);
    visit(5508, foreground);
    visit(5509, metrics);
    visit(5510, rise);
    visit(5511, strikeout);
    visit(5512, strikeoutColor);
    visit(5513, underline);
    visit(5514, underlineColor);
    visit(5515, underlineStyle);
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = borderStyle;
      if (v_ != null) {
        w.keyInt32(5506, v_);
      }
    }
    {
      final v_ = rise;
      if (v_ != null) {
        w.keyInt32(5510, v_);
      }
    }
    {
      final v_ = strikeout;
      if (v_ != null) {
        w.keyBoolean(5511, v_);
      }
    }
    {
      final v_ = underline;
      if (v_ != null) {
        w.keyBoolean(5513, v_);
      }
    }
    {
      final v_ = underlineStyle;
      if (v_ != null) {
        w.keyInt32(5515, v_);
      }
    }
  }
}
