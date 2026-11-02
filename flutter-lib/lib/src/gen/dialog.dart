import '../comm/widget_wire.dart';
import 'widgets.dart' show WireKey;

/// A dialog a Shell holds, read from the element of its `dialogs` list a frame carries.
class VDialog implements WireReadable, WireWritable, WireVisitable {
  VDialog() : this.empty();
  VDialog.empty();

  String swt = '';
  int id = 0;
  int? style;
  String? text;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case WireKey.k_Dialog_id:
        id = r.int64();
      case WireKey.k_Dialog_style:
        style = r.int32();
      case WireKey.k_Dialog_swt:
        swt = r.string() ?? '';
      case WireKey.k_Dialog_text:
        text = r.string();
      default:
        return false;
    }
    return true;
  }

  @override
  void writeWire(WidgetWireWriter w) {
    w.key(WireKey.k_Dialog_id);
    w.int64(id);
    final style = this.style;
    if (style != null) {
      w.key(WireKey.k_Dialog_style);
      w.int32(style);
    }
    w.key(WireKey.k_Dialog_swt);
    w.string(swt);
    final text = this.text;
    if (text != null) {
      w.key(WireKey.k_Dialog_text);
      w.string(text);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    visit(WireKey.k_Dialog_id, id);
    visit(WireKey.k_Dialog_style, style);
    visit(WireKey.k_Dialog_swt, swt);
    visit(WireKey.k_Dialog_text, text);
  }
}
