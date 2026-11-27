import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/filedialog_evolve.dart';
import 'package:swtflutter/src/impl/utils/dialog_button.dart';
import 'package:swtflutter/src/theme/theme.dart';

const _sep = choiceSeparator;

/// A server file system of two directories: /home with a subdirectory and three files, and
/// /home/docs with one file. The filter index picks `*.txt` (0) or everything (1).
Future<ServerListing> _fs(String path, int filterIndex) async {
  if (path == '/home/docs') {
    return const ServerListing(path: '/home/docs', parent: '/home', roots: ['/'], files: ['c.txt']);
  }
  return ServerListing(
    path: '/home',
    parent: '/',
    roots: const ['/'],
    dirs: const ['docs'],
    files: filterIndex == 0 ? const ['a.txt', 'b.txt'] : const ['a.txt', 'b.txt', 'z.bin'],
  );
}

Future<List<String?>> _pump(WidgetTester tester, ChooserMode mode,
    {String fileName = '',
    bool overwrite = false,
    Future<ServerListing> Function(String path, String name, int filterIndex)? createFolder}) async {
  final results = <String?>[];
  await tester.pumpWidget(MaterialApp(
    theme: createLightNonDefaultTheme(null),
    home: Scaffold(
      body: FileDialogContent(
        key: UniqueKey(),
        overwrite: overwrite,
        title: 'Open Trace',
        mode: mode,
        startPath: '/home',
        fileName: fileName,
        filterNames: const ['Text', 'All'],
        filterExtensions: const ['*.txt', '*'],
        list: _fs,
        createFolder: createFolder,
        onResult: results.add,
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return results;
}

bool _okEnabled(WidgetTester tester) =>
    tester.widget<DialogButton>(find.byKey(const ValueKey('fileDialog.ok'))).onPressed != null;

void main() {
  testWidgets('an OPEN chooser has no name field and Open waits for a picked file', (tester) async {
    for (final mode in [ChooserMode.open, ChooserMode.multi]) {
      await _pump(tester, mode, fileName: 'a.txt');
      expect(find.byKey(const ValueKey('fileDialog.name')), findsNothing, reason: '$mode');
      expect(find.byKey(const ValueKey('fileDialog.filter')), findsOneWidget, reason: '$mode');
      expect(_okEnabled(tester), isFalse, reason: '$mode');

      await tester.tap(find.text('a.txt'));
      await tester.pump();
      expect(_okEnabled(tester), isTrue, reason: '$mode');
    }
  });

  testWidgets('a SAVE chooser has a name field and Save waits for a name', (tester) async {
    await _pump(tester, ChooserMode.save);
    expect(_okEnabled(tester), isFalse);

    await tester.enterText(find.byKey(const ValueKey('fileDialog.name')), 'out.txt');
    await tester.pump();
    expect(_okEnabled(tester), isTrue);

    await tester.tap(find.text('b.txt'));
    await tester.pump();
    expect(find.widgetWithText(TextField, 'b.txt'), findsOneWidget);
  });

  testWidgets('New Folder makes the folder named in the current directory and shows it', (tester) async {
    final asked = <String>[];
    final results = await _pump(tester, ChooserMode.open, createFolder: (path, name, index) async {
      asked.add('$path|$name|$index');
      return ServerListing(path: '$path/$name', parent: path);
    });

    await tester.tap(find.byKey(const ValueKey('fileDialog.newFolder')));
    await tester.pumpAndSettle();
    expect(find.widgetWithText(TextField, 'untitled folder'), findsOneWidget);
    await tester.enterText(find.byKey(const ValueKey('fileDialog.newFolder.name')), 'made');
    await tester.tap(find.byKey(const ValueKey('fileDialog.newFolder.create')));
    await tester.pumpAndSettle();

    expect(asked, ['/home|made|0']);
    expect(find.text('/home/made'), findsOneWidget);
    expect(find.byKey(const ValueKey('fileDialog.newFolder.failed')), findsNothing);
    expect(results, isEmpty);
  });

  testWidgets('a New Folder that cannot be made says so; a cancelled one asks nothing', (tester) async {
    final asked = <String>[];
    await _pump(tester, ChooserMode.directory, createFolder: (path, name, index) async {
      asked.add(name);
      return ServerListing(path: path, parent: '/', dirs: const ['docs'], created: false);
    });

    await tester.tap(find.byKey(const ValueKey('fileDialog.newFolder')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const ValueKey('fileDialog.newFolder.cancel')));
    await tester.pumpAndSettle();
    expect(asked, isEmpty);

    await tester.tap(find.byKey(const ValueKey('fileDialog.newFolder')));
    await tester.pumpAndSettle();
    await tester.enterText(find.byKey(const ValueKey('fileDialog.newFolder.name')), 'docs');
    await tester.tap(find.byKey(const ValueKey('fileDialog.newFolder.create')));
    await tester.pumpAndSettle();

    expect(asked, ['docs']);
    expect(find.byKey(const ValueKey('fileDialog.newFolder.failed')), findsOneWidget);
    expect(find.text('/home'), findsOneWidget);
  });

  testWidgets('a chooser given no way to make folders has no New Folder button', (tester) async {
    await _pump(tester, ChooserMode.open);
    expect(find.byKey(const ValueKey('fileDialog.newFolder')), findsNothing);
  });

  testWidgets('an open chooser lists the start directory and answers the picked file', (tester) async {
    final results = await _pump(tester, ChooserMode.open);
    expect(find.text('/home'), findsOneWidget);
    expect(find.text('z.bin'), findsNothing);

    await tester.tap(find.text('b.txt'));
    await tester.pump();
    await tester.tap(find.byKey(const ValueKey('fileDialog.ok')));

    expect(results, ['0$_sep/home${_sep}b.txt$_sep']);
  });

  testWidgets('a directory opens on tap and Up goes back to its parent', (tester) async {
    final results = await _pump(tester, ChooserMode.open);

    await tester.tap(find.text('docs'));
    await tester.pumpAndSettle();
    expect(find.text('/home/docs'), findsOneWidget);
    expect(find.text('c.txt'), findsOneWidget);

    await tester.tap(find.byKey(const ValueKey('fileDialog.up')));
    await tester.pumpAndSettle();
    expect(find.text('/home'), findsOneWidget);
    expect(results, isEmpty);
  });

  testWidgets('another filter lists the files it matches and is answered with them', (tester) async {
    final results = await _pump(tester, ChooserMode.open);

    await tester.tap(find.byKey(const ValueKey('fileDialog.filter')));
    await tester.pumpAndSettle();
    await tester.tap(find.text('All').last);
    await tester.pumpAndSettle();
    await tester.tap(find.text('z.bin'));
    await tester.pump();
    await tester.tap(find.byKey(const ValueKey('fileDialog.ok')));

    expect(results, ['1$_sep/home${_sep}z.bin$_sep']);
  });

  testWidgets('a MULTI chooser answers every file picked, in listing order', (tester) async {
    final results = await _pump(tester, ChooserMode.multi);

    await tester.tap(find.text('b.txt'));
    await tester.tap(find.text('a.txt'));
    await tester.pump();
    await tester.tap(find.byKey(const ValueKey('fileDialog.ok')));

    expect(results, ['0$_sep/home${_sep}a.txt${_sep}b.txt$_sep']);
  });

  testWidgets('a SAVE chooser answers the typed name, and starts with the one it was given', (tester) async {
    final results = await _pump(tester, ChooserMode.save, fileName: 'out.txt');
    expect(find.widgetWithText(TextField, 'out.txt'), findsOneWidget);

    await tester.enterText(find.byKey(const ValueKey('fileDialog.name')), 'new.txt');
    await tester.pump();
    await tester.tap(find.byKey(const ValueKey('fileDialog.ok')));

    expect(results, ['0$_sep/home${_sep}new.txt$_sep']);
  });

  testWidgets('a directory chooser lists no files and answers the directory picked', (tester) async {
    final results = <String?>[];
    await tester.pumpWidget(MaterialApp(
      theme: createLightNonDefaultTheme(null),
      home: Scaffold(
        body: FileDialogContent(
          title: 'Directory',
          mode: ChooserMode.directory,
          startPath: '/home',
          list: (p, i) async => const ServerListing(path: '/home', parent: '/', dirs: ['docs']),
          onResult: results.add,
        ),
      ),
    ));
    await tester.pumpAndSettle();
    expect(find.byKey(const ValueKey('fileDialog.name')), findsNothing);

    await tester.tap(find.text('docs'));
    await tester.pump();
    await tester.tap(find.byKey(const ValueKey('fileDialog.ok')));

    expect(results, ['0$_sep/home/docs$_sep']);
  });

  testWidgets('a SAVE chooser with overwrite set asks before it answers an existing name', (tester) async {
    final results = await _pump(tester, ChooserMode.save, fileName: 'a.txt', overwrite: true);

    await tester.tap(find.byKey(const ValueKey('fileDialog.ok')));
    await tester.pumpAndSettle();
    expect(results, isEmpty);
    await tester.tap(find.byKey(const ValueKey('fileDialog.overwrite.cancel')));
    await tester.pumpAndSettle();
    expect(results, isEmpty, reason: 'declining the replace keeps the chooser open');

    await tester.tap(find.byKey(const ValueKey('fileDialog.ok')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const ValueKey('fileDialog.overwrite.replace')));
    await tester.pumpAndSettle();
    expect(results, ['0$_sep/home${_sep}a.txt$_sep']);
  });

  testWidgets('a SAVE chooser asks nothing for a new name, or with overwrite unset', (tester) async {
    var results = await _pump(tester, ChooserMode.save, fileName: 'new.txt', overwrite: true);
    await tester.tap(find.byKey(const ValueKey('fileDialog.ok')));
    await tester.pumpAndSettle();
    expect(results, ['0$_sep/home${_sep}new.txt$_sep']);

    results = await _pump(tester, ChooserMode.save, fileName: 'a.txt');
    await tester.tap(find.byKey(const ValueKey('fileDialog.ok')));
    await tester.pumpAndSettle();
    expect(find.byKey(const ValueKey('fileDialog.overwrite.replace')), findsNothing);
    expect(results, ['0$_sep/home${_sep}a.txt$_sep']);
  });

  testWidgets('Cancel answers null', (tester) async {
    final results = await _pump(tester, ChooserMode.open);
    await tester.tap(find.byKey(const ValueKey('fileDialog.cancel')));
    expect(results, [null]);
  });

  test('a listing reads from the tagged map Java sends', () {
    final l = ServerListing.fromMap({
      'path': '/home',
      'parent': '/',
      'separator': '/',
      'roots': ['/'],
      'dirs': ['docs'],
      'files': ['a.txt'],
    });
    expect([l.path, l.parent, l.roots, l.dirs, l.files], ['/home', '/', ['/'], ['docs'], ['a.txt']]);
  });
}
