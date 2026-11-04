import '../comm/widget_wire.dart';
import '../gen/font.dart';
import '../gen/pathdata.dart';
import '../gen/widgets.dart';

class VPath implements WireReadable, WireWritable, WireVisitable {
  VPath() : this.empty();
  VPath.empty();

  VPathData? pathData;

  List<String?>? textStrings;
  List<double>? textOrigins;
  List<VFont?>? textFonts;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 392: // pathData
        pathData = r.object(VPathData.new);
        return true;
      case 395: // textStrings
        textStrings = r.strings();
        return true;
      case 394: // textOrigins
        textOrigins = r.floats();
        return true;
      case 393: // textFonts
        textFonts = r.list((r) => r.object(VFont.new));
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = pathData;
      if (v_ != null) {
        w.key(392);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = textStrings;
      if (v_ != null) {
        w.key(395);
        w.strings(v_);
      }
    }
    {
      final v_ = textOrigins;
      if (v_ != null) {
        w.key(394);
        w.floats(v_);
      }
    }
    {
      final v_ = textFonts;
      if (v_ != null) {
        w.key(393);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(392, pathData);
    visit(395, textStrings);
    visit(394, textOrigins);
    visit(393, textFonts);
  }
}
