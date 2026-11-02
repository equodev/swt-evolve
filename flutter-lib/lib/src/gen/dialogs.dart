import 'colordialog.dart';
import 'dialog.dart';
import 'messagebox.dart';

/// An empty dialog value of the class a binary frame names, for it to be read into.
Object? newDialogValue(String name) => switch (name) {
  'MessageBox' => VMessageBox(),
  'ColorDialog' => VColorDialog(),
  _ => VDialog(),
};
