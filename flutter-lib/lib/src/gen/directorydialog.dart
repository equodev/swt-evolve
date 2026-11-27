import '../comm/widget_wire.dart';
import '../gen/dialog.dart';
import 'widgets.dart' show WireKey;

class VDirectoryDialog extends VDialog {
  VDirectoryDialog() : this.empty();
  VDirectoryDialog.empty() : super.empty();

  String? filterPath;
  String? message;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case WireKey.k_DirectoryDialog_filterPath:
        filterPath = r.string();
      case WireKey.k_DirectoryDialog_message:
        message = r.string();
      default:
        return super.readWire(key, r);
    }
    return true;
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    final filterPath = this.filterPath;
    if (filterPath != null) {
      w.key(WireKey.k_DirectoryDialog_filterPath);
      w.string(filterPath);
    }
    final message = this.message;
    if (message != null) {
      w.key(WireKey.k_DirectoryDialog_message);
      w.string(message);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(WireKey.k_DirectoryDialog_filterPath, filterPath);
    visit(WireKey.k_DirectoryDialog_message, message);
  }
}
