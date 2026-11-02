import '../comm/widget_wire.dart';
import '../gen/dialog.dart';
import '../gen/rgb.dart';
import 'widgets.dart' show WireKey;

class VColorDialog extends VDialog {
  VColorDialog() : this.empty();
  VColorDialog.empty() : super.empty();

  VRGB? rgb;
  List<VRGB>? rgbs;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case WireKey.k_ColorDialog_RGB:
        rgb = r.rgb();
      case WireKey.k_ColorDialog_RGBs:
        rgbs = r.list((r) => r.rgb())?.whereType<VRGB>().toList();
      default:
        return super.readWire(key, r);
    }
    return true;
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    final rgb = this.rgb;
    if (rgb != null) {
      w.key(WireKey.k_ColorDialog_RGB);
      w.rgbValue(rgb);
    }
    final rgbs = this.rgbs;
    if (rgbs != null) {
      w.key(WireKey.k_ColorDialog_RGBs);
      w.list(rgbs, w.rgbValue);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(WireKey.k_ColorDialog_RGB, rgb);
    visit(WireKey.k_ColorDialog_RGBs, rgbs);
  }
}
