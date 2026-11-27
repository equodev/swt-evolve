// The web file chooser is drawn from the active theme like the other Evolve dialogs: its chrome
// from FileDialogThemeExtension, its rows from the List theme, its buttons from the Button theme.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/filedialog_evolve.dart';
import 'package:swtflutter/src/theme/theme.dart';
import 'package:swtflutter/src/theme/theme_extensions/button_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/filedialog_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_extensions/list_theme_extension.dart';

Future<ServerListing> _fs(String path, int filterIndex) async =>
    const ServerListing(path: '/home', parent: '/', roots: ['/'], dirs: ['docs'], files: ['a.txt']);

Future<ThemeData> _pump(WidgetTester tester, ThemeData theme) async {
  await tester.pumpWidget(MaterialApp(
    theme: theme,
    home: Scaffold(
      body: FileDialogContent(
        key: UniqueKey(),
        title: 'Open Trace',
        mode: ChooserMode.open,
        startPath: '/home',
        list: _fs,
        onResult: (_) {},
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return theme;
}

Color? _surface(WidgetTester tester) => tester
    .widget<Material>(find.descendant(of: find.byType(FileDialogContent), matching: find.byType(Material)).first)
    .color;

Color? _textColor(WidgetTester tester, String text) => tester.widget<Text>(find.text(text)).style?.color;

void main() {
  for (final (name, make) in [
    ('light', () => createLightNonDefaultTheme(null)),
    ('dark', () => createDarkNonDefaultTheme(null)),
  ]) {
    testWidgets('the chooser takes its look from the $name theme', (tester) async {
      final theme = await _pump(tester, make());
      final dialog = theme.extension<FileDialogThemeExtension>()!;
      final list = theme.extension<ListThemeExtension>()!;
      final button = theme.extension<ButtonThemeExtension>()!;

      expect(dialog.backgroundColor, theme.colorScheme.surface);
      expect(_surface(tester), dialog.backgroundColor);
      expect(_textColor(tester, 'Open Trace'), dialog.titleStyle?.color);
      expect(_textColor(tester, '/home'), dialog.pathStyle?.color);
      expect(_textColor(tester, 'a.txt'), list.textColor);
      expect(_textColor(tester, 'Open'), button.disabledForegroundColor);

      await tester.tap(find.text('a.txt'));
      await tester.pump();
      expect(_textColor(tester, 'a.txt'), list.selectedItemTextColor);
      expect(_textColor(tester, 'Open'), button.pushButtonTextColor);
      expect(_textColor(tester, 'Cancel'), button.secondaryButtonTextColor);
    });
  }

  testWidgets('a dark theme gives the chooser the dark surface', (tester) async {
    await _pump(tester, createLightNonDefaultTheme(null));
    final light = _surface(tester);
    final dark = (await _pump(tester, createDarkNonDefaultTheme(null))).colorScheme.surface;
    expect(_surface(tester), dark);
    expect(dark, isNot(light));
  });

  testWidgets('the list border encloses the rows', (tester) async {
    final theme = await _pump(tester, createLightNonDefaultTheme(null));
    final list = theme.extension<ListThemeExtension>()!;
    final listView = find.byKey(const ValueKey('fileDialog.list'));
    final box = find.ancestor(of: listView, matching: find.byType(DecoratedBox)).first;
    final decoration = tester.widget<DecoratedBox>(box).decoration as BoxDecoration;
    expect(decoration.border, Border.all(color: list.borderColor, width: list.borderWidth));

    final inside = tester.getRect(box).deflate(list.borderWidth);
    for (final name in ['docs', 'a.txt']) {
      final row = tester.getRect(find.ancestor(of: find.text(name), matching: find.byType(GestureDetector)).first);
      expect(inside.contains(row.topLeft) && inside.contains(row.bottomRight - const Offset(0.01, 0.01)), isTrue,
          reason: '$name row $row lies outside the border\'s inside $inside');
    }
  });
}
