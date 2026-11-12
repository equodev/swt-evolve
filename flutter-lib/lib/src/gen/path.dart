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
      case 397: // pathData
        pathData = r.object(VPathData.new);
        return true;
      case 400: // textStrings
        textStrings = r.strings();
        return true;
      case 399: // textOrigins
        textOrigins = r.floats();
        return true;
      case 398: // textFonts
        textFonts = r.objects(VFont.new);
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
        w.key(397);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = textStrings;
      if (v_ != null) {
        w.key(400);
        w.strings(v_);
      }
    }
    {
      final v_ = textOrigins;
      if (v_ != null) {
        w.key(399);
        w.floats(v_);
      }
    }
    {
      final v_ = textFonts;
      if (v_ != null) {
        w.key(398);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(397, pathData);
    visit(400, textStrings);
    visit(399, textOrigins);
    visit(398, textFonts);
  }
}
