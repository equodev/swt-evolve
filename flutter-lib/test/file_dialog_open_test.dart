// A FileDialog or DirectoryDialog parked on a Shell opens the chooser over the server's file system,
// started from what the Java dialog was given.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/dialog.dart';
import 'package:swtflutter/src/gen/directorydialog.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/filedialog.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/filedialog_evolve.dart';

VShell _shell({List<VDialog>? dialogs}) => VShell()
  ..id = 1
  ..style = SWT.SHELL_TRIM
  ..text = 'shell'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 800
    ..height = 600)
  ..dialogs = dialogs;

Future<FileDialogContent> _open(WidgetTester tester, VDialog dialog) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(value: VDisplay()..shells = [_shell()]),
  ));
  await tester.pumpAndSettle();
  final shellState = tester.state(find.byType(ShellSwt<VShell>)) as dynamic;
  shellState.setValue(_shell(dialogs: [dialog]));
  await tester.pump();
  await tester.pump();
  expect(find.byType(FileDialogContent), findsOneWidget);
  return tester.widget<FileDialogContent>(find.byType(FileDialogContent));
}

void main() {
  testWidgets('a FileDialog on the Shell opens the chooser where the dialog was set to start',
      (WidgetTester tester) async {
    final content = await _open(
        tester,
        VFileDialog()
          ..id = 4243
          ..swt = 'FileDialog'
          ..style = SWT.SAVE | SWT.APPLICATION_MODAL
          ..filterPath = '/data'
          ..fileName = 'out.csv'
          ..filterNames = ['CSV', null]
          ..filterExtensions = ['*.csv', null]
          ..filterIndex = -1
          ..overwrite = true);

    expect(content.mode, ChooserMode.save);
    expect(content.title, 'Save');
    expect(content.startPath, '/data');
    expect(content.fileName, 'out.csv');
    expect(content.filterNames, ['CSV']);
    expect(content.filterExtensions, ['*.csv']);
    expect(content.filterIndex, 0, reason: 'SWT starts on the first filter when none was chosen');
    expect(content.overwrite, isTrue);
  });

  testWidgets('a DirectoryDialog on the Shell opens the chooser on directories',
      (WidgetTester tester) async {
    final content = await _open(
        tester,
        VDirectoryDialog()
          ..id = 4244
          ..swt = 'DirectoryDialog'
          ..style = SWT.APPLICATION_MODAL
          ..text = 'Pick a folder'
          ..message = 'Where to export'
          ..filterPath = '/data');

    expect(content.mode, ChooserMode.directory);
    expect(content.title, 'Pick a folder');
    expect(content.message, 'Where to export');
    expect(content.startPath, '/data');
  });

  test('the chooser mode follows the FileDialog style', () {
    expect(fileDialogMode(SWT.OPEN), ChooserMode.open);
    expect(fileDialogMode(SWT.OPEN | SWT.MULTI), ChooserMode.multi);
    expect(fileDialogMode(SWT.SAVE), ChooserMode.save);
  });
}
