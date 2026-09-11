// A menu assembled from contributions routinely carries separators that separate nothing: each
// contributor adds its own trailing one, and whichever groups turn out empty leave theirs behind. A
// platform menu does not draw those. The same menu on native SWT showed four lines where the item
// list carried seven, with none above the first row.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/menuitem.dart' show MenuItemSwt;
import 'package:swtflutter/src/gen/swt.dart';

import 'support/menu_shown_ack.dart';

VMenuItem _sep(int id) => VMenuItem()
  ..id = id
  ..style = SWT.SEPARATOR
  ..enabled = true;

VMenuItem _item(int id, String text) => VMenuItem()
  ..id = id
  ..style = SWT.PUSH
  ..enabled = true
  ..text = text;

/// The shape the reported application produces: a leading separator, a run of three in the middle,
/// and a trailing one.
VMenu _asContributed() => VMenu()
  ..id = 100
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = true
  ..items = [
    _sep(1),
    _item(2, 'Section View Editor...'),
    _sep(3),
    _item(4, 'Toggle On/Off Full Screen Mode'),
    _sep(5),
    _sep(6),
    _sep(7),
    _item(8, 'Rename Editor'),
    _sep(9),
  ];

/// Separators are the mounted MenuItemSwt widgets carrying SWT.SEPARATOR.
int _separatorsOnScreen(WidgetTester tester) => tester
    .widgetList<MenuItemSwt>(find.byType(MenuItemSwt))
    .where((w) => (w.value.style & SWT.SEPARATOR) != 0)
    .length;

Future<void> _open(WidgetTester tester, VMenu menu) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 300,
      child: Stack(children: [Positioned.fill(child: MenuSwt<VMenu>(value: menu))]),
    ),
  ));
  await tester.pump();
  await ackMenuShown(tester, 100);
}

void main() {
  testWidgets('a run of separators draws as one line', (tester) async {
    await _open(tester, _asContributed());

    expect(_separatorsOnScreen(tester), 2,
        reason: 'three items make room for two lines, not five');
  });

  testWidgets('every item still renders', (tester) async {
    await _open(tester, _asContributed());

    expect(find.text('Section View Editor...'), findsOneWidget);
    expect(find.text('Toggle On/Off Full Screen Mode'), findsOneWidget);
    expect(find.text('Rename Editor'), findsOneWidget);
  });

  testWidgets('a menu that is nothing but separators draws none of them',
      (tester) async {
    await _open(
      tester,
      VMenu()
        ..id = 100
        ..style = SWT.POP_UP
        ..enabled = true
        ..visible = true
        ..items = [_sep(1), _sep(2), _sep(3)],
    );

    expect(_separatorsOnScreen(tester), 0);
  });
}
