import '../comm/widget_wire.dart';
import '../gen/dialog.dart';
import 'widgets.dart' show WireKey;

class VFileDialog extends VDialog {
  VFileDialog() : this.empty();
  VFileDialog.empty() : super.empty();

  String? fileName;
  List<String?>? fileNames;
  List<String?>? filterExtensions;
  int? filterIndex;
  List<String?>? filterNames;
  String? filterPath;
  bool? overwrite;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case WireKey.k_FileDialog_fileName:
        fileName = r.string();
      case WireKey.k_FileDialog_fileNames:
        fileNames = r.strings();
      case WireKey.k_FileDialog_filterExtensions:
        filterExtensions = r.strings();
      case WireKey.k_FileDialog_filterIndex:
        filterIndex = r.int32();
      case WireKey.k_FileDialog_filterNames:
        filterNames = r.strings();
      case WireKey.k_FileDialog_filterPath:
        filterPath = r.string();
      case WireKey.k_FileDialog_overwrite:
        overwrite = r.boolean();
      default:
        return super.readWire(key, r);
    }
    return true;
  }

  @override
  void writeWire(WidgetWireWriter w) {
    super.writeWire(w);
    final fileName = this.fileName;
    if (fileName != null) {
      w.key(WireKey.k_FileDialog_fileName);
      w.string(fileName);
    }
    final fileNames = this.fileNames;
    if (fileNames != null) {
      w.key(WireKey.k_FileDialog_fileNames);
      w.strings(fileNames);
    }
    final filterExtensions = this.filterExtensions;
    if (filterExtensions != null) {
      w.key(WireKey.k_FileDialog_filterExtensions);
      w.strings(filterExtensions);
    }
    final filterIndex = this.filterIndex;
    if (filterIndex != null) {
      w.key(WireKey.k_FileDialog_filterIndex);
      w.int32(filterIndex);
    }
    final filterNames = this.filterNames;
    if (filterNames != null) {
      w.key(WireKey.k_FileDialog_filterNames);
      w.strings(filterNames);
    }
    final filterPath = this.filterPath;
    if (filterPath != null) {
      w.key(WireKey.k_FileDialog_filterPath);
      w.string(filterPath);
    }
    final overwrite = this.overwrite;
    if (overwrite != null) {
      w.key(WireKey.k_FileDialog_overwrite);
      w.boolean(overwrite);
    }
  }

  @override
  void visitWire(void Function(int key, Object? value) visit) {
    super.visitWire(visit);
    visit(WireKey.k_FileDialog_fileName, fileName);
    visit(WireKey.k_FileDialog_fileNames, fileNames);
    visit(WireKey.k_FileDialog_filterExtensions, filterExtensions);
    visit(WireKey.k_FileDialog_filterIndex, filterIndex);
    visit(WireKey.k_FileDialog_filterNames, filterNames);
    visit(WireKey.k_FileDialog_filterPath, filterPath);
    visit(WireKey.k_FileDialog_overwrite, overwrite);
  }
}
