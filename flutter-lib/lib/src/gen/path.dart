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
      case 393: // pathData
        pathData = r.object(VPathData.new);
        return true;
      case 396: // textStrings
        textStrings = r.strings();
        return true;
      case 395: // textOrigins
        textOrigins = r.floats();
        return true;
      case 394: // textFonts
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
        w.key(393);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = textStrings;
      if (v_ != null) {
        w.key(396);
        w.strings(v_);
      }
    }
    {
      final v_ = textOrigins;
      if (v_ != null) {
        w.key(395);
        w.floats(v_);
      }
    }
    {
      final v_ = textFonts;
      if (v_ != null) {
        w.key(394);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(393, pathData);
    visit(396, textStrings);
    visit(395, textOrigins);
    visit(394, textFonts);
  }
}
