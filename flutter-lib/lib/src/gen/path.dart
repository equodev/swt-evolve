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
      case 389: // pathData
        pathData = r.object(VPathData.new);
        return true;
      case 392: // textStrings
        textStrings = r.strings();
        return true;
      case 391: // textOrigins
        textOrigins = r.floats();
        return true;
      case 390: // textFonts
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
        w.key(389);
        w.object(v_.writeWire);
      }
    }
    {
      final v_ = textStrings;
      if (v_ != null) {
        w.key(392);
        w.strings(v_);
      }
    }
    {
      final v_ = textOrigins;
      if (v_ != null) {
        w.key(391);
        w.floats(v_);
      }
    }
    {
      final v_ = textFonts;
      if (v_ != null) {
        w.key(390);
        w.list(v_, (e) => w.object(e?.writeWire));
      }
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(389, pathData);
    visit(392, textStrings);
    visit(391, textOrigins);
    visit(390, textFonts);
  }
}
