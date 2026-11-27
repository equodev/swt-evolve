import 'package:flutter/widgets.dart';

import '../gen/colordialog.dart';
import '../gen/dialog.dart';
import '../gen/directorydialog.dart';
import '../gen/filedialog.dart';
import '../gen/messagebox.dart';
import '../gen/shell.dart';
import 'colordialog_evolve.dart';
import 'filedialog_evolve.dart';
import 'messagebox_evolve.dart';

/// The dialogs already shown for each shell record, by dialog id. The shell draws its dialogs and
/// the display those of a shell it does not draw; sharing this keeps a dialog to one box.
final Expando<Set<int>> _shown = Expando();

/// Shows over [context], after this frame, each dialog [shell] holds that was not shown yet, and
/// forgets the ones Java has dropped: one Java adds back is shown as a new one.
void openNewDialogs(BuildContext context, VShell shell) {
  final dialogs = shell.dialogs ?? const <VDialog>[];
  final shown = _shown[shell] ??= {};
  shown.retainWhere((id) => dialogs.any((d) => d.id == id));
  for (final d in dialogs) {
    if (!shown.add(d.id)) continue;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (context.mounted) _openDialog(context, d);
    });
    WidgetsBinding.instance.ensureVisualUpdate();
  }
}

void _openDialog(BuildContext context, VDialog d) {
  switch (d.swt) {
    case 'MessageBox':
      showMessageBoxDialog(context, d as VMessageBox, d.id);
    case 'ColorDialog':
      showColorDialog(context, d as VColorDialog, d.id);
    case 'FileDialog':
      showFileDialog(context, d as VFileDialog, d.id);
    case 'DirectoryDialog':
      showDirectoryDialog(context, d as VDirectoryDialog, d.id);
  }
}
