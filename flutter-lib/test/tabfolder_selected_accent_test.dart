// The selected tab carries the theme's accent on the edge next to the page, over the strip's line:
// the bottom edge of a TOP folder, the top edge of a BOTTOM one.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/tabfolder_evolve.dart';
import 'package:swtflutter/src/theme/theme_extensions/tabfolder_theme_extension.dart';

VTabFolder _folder(int id, int style) {
  final folder = VTabFolder()
    ..id = id
    ..style = style
    ..enabled = true
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 300
      ..height = 120)
    ..items = [
      VTabItem()
        ..id = id + 1
        ..text = 'One',
      VTabItem()
        ..id = id + 2
        ..text = 'Two',
    ];
  folder.selection = [folder.items!.first];
  return folder;
}

Future<void> _pump(WidgetTester tester, VTabFolder value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 300, height: 120, child: TabFolderSwt<VTabFolder>(value: value)),
    ),
  ));
  await tester.pump();
  while (tester.takeException() != null) {}
}

/// The decorated box of the tab at [index].
Finder _tab(int index) =>
    find.descendant(of: find.byType(InkWell).at(index), matching: find.byType(Container)).first;

Border _border(WidgetTester tester, int index) =>
    (tester.widget<Container>(_tab(index)).decoration as BoxDecoration).border as Border;

void main() {
  testWidgets('a TOP folder draws the accent on the selected tab\'s bottom edge, over the strip line',
      (tester) async {
    await _pump(tester, _folder(10, SWT.TOP));
    final theme = Theme.of(tester.element(_tab(0))).extension<TabFolderThemeExtension>()!;

    expect(_border(tester, 0).bottom.width, theme.tabSelectedBorderWidth);
    expect(_border(tester, 1).bottom.width, theme.tabBorderWidth);
    // The strip's line is the band just above the page; the accent covers it.
    final page = tester.getRect(find.byType(IndexedStack));
    final accent = tester.getRect(_tab(0));
    expect(accent.bottom, greaterThanOrEqualTo(page.top));
    expect(accent.bottom - theme.tabSelectedBorderWidth,
        lessThanOrEqualTo(page.top - theme.tabBorderWidth));
  });

  testWidgets('a BOTTOM folder draws the accent on the selected tab\'s top edge, over the strip line',
      (tester) async {
    await _pump(tester, _folder(20, SWT.BOTTOM));
    final theme = Theme.of(tester.element(_tab(0))).extension<TabFolderThemeExtension>()!;

    expect(_border(tester, 0).top.width, theme.tabSelectedBorderWidth);
    expect(_border(tester, 1).top.width, theme.tabBorderWidth);
    // The strip's line is the band just below the page; the accent covers it.
    final page = tester.getRect(find.byType(IndexedStack));
    final accent = tester.getRect(_tab(0));
    expect(accent.top, lessThanOrEqualTo(page.bottom));
    expect(accent.top + theme.tabSelectedBorderWidth,
        greaterThanOrEqualTo(page.bottom + theme.tabBorderWidth));
  });

  testWidgets('the accent leaves the strip as tall at the bottom as at the top', (tester) async {
    await _pump(tester, _folder(30, SWT.TOP));
    final top = tester.getSize(find.byType(IndexedStack)).height;
    await _pump(tester, _folder(40, SWT.BOTTOM));
    expect(tester.getSize(find.byType(IndexedStack)).height, top);
  });
}
