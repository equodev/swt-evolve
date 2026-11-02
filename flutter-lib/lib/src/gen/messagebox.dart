import '../comm/widget_wire.dart';
import '../gen/dialog.dart';
import 'widgets.dart' show WireKey;

class VMessageBox extends VDialog {
  VMessageBox() : this.empty();
  VMessageBox.empty() : super.empty();

  String? message;
  // The label of each button a message box can show, in the order OK, Cancel, Yes, No, Abort,
  // Retry, Ignore.
  List<String?>? buttonLabels;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case WireKey.k_MessageBox_message:
        message = r.string();
      case WireKey.k_MessageBox_buttonLabels:
        buttonLabels = r.strings();
      default:
        return super.readWire(key, r);
    }
    return true;
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    final message = this.message;
    if (message != null) {
      w.key(WireKey.k_MessageBox_message);
      w.string(message);
    }
    final buttonLabels = this.buttonLabels;
    if (buttonLabels != null) {
      w.key(WireKey.k_MessageBox_buttonLabels);
      w.strings(buttonLabels);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(WireKey.k_MessageBox_message, message);
    visit(WireKey.k_MessageBox_buttonLabels, buttonLabels);
  }
}
