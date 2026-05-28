import 'dialog.dart';
import 'messagebox.dart';

VDialog mapDialogValue(Map<String, dynamic> m) => switch (m['swt'] as String?) {
  'MessageBox' => VMessageBox.fromJson(m),
  _ => VDialog.fromJson(m),
};

List<VDialog>? parseDialogs(dynamic raw) {
  if (raw == null) return null;
  return (raw as List).map((e) => mapDialogValue(e as Map<String, dynamic>)).toList();
}
