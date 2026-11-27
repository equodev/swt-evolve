import 'colordialog.dart';
import 'dialog.dart';
import 'directorydialog.dart';
import 'filedialog.dart';
import 'messagebox.dart';

/// An empty dialog value of the class a binary frame names, for it to be read into.
Object? newDialogValue(String name) => switch (name) {
  'MessageBox' => VMessageBox(),
  'ColorDialog' => VColorDialog(),
  'FileDialog' => VFileDialog(),
  'DirectoryDialog' => VDirectoryDialog(),
  _ => VDialog(),
};
