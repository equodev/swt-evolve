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
      case 565: // pathData
        pathData = r.object(VPathData.new);
        return true;
      case 566: // textStrings
        textStrings = r.strings();
        return true;
      case 567: // textOrigins
        textOrigins = r.floats();
        return true;
      case 568: // textFonts
        textFonts = r.list(() => r.object(VFont.new));
        return true;
      default:
        return false;
    }
  }

  @override
  void writeWire(WidgetWireWriter w) {
    {
      final v_ = pathData;
      if (v_ != null && w.wants(565)) {
        w.key(565);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = textStrings;
      if (v_ != null && w.wants(566)) {
        w.key(566);
        w.strings(v_);
      }
    }
    {
      final v_ = textOrigins;
      if (v_ != null && w.wants(567)) {
        w.key(567);
        w.floats(v_);
      }
    }
    {
      final v_ = textFonts;
      if (v_ != null && w.wants(568)) {
        w.key(568);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(565, pathData);
    visit(566, textStrings);
    visit(567, textOrigins);
    visit(568, textFonts);
  }
}
