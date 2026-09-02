import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

import 'support/menu_detect_ack.dart';
import 'support/menu_shown_ack.dart';

VMenu _menu(int id) => VMenu()
  ..id = id
  ..style = SWT.POP_UP
  ..enabled = true
  ..items = [
    VMenuItem()
      ..id = id + 1
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Insert...',
  ];

VTable _table(VMenu menu) => VTable()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..headerVisible = false
  ..menu = menu
  ..columns = [
    VTableColumn()
      ..id = 2
      ..width = 200
      ..alignment = SWT.LEFT
      ..text = 'Column'
  ]
  ..items = [
    VTableItem()
      ..id = 10
      ..texts = ['row 0'],
    VTableItem()
      ..id = 11
      ..texts = ['row 1'],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 200);

Widget _wrap(VTable value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: 200,
        child: TableSwt<VTable>(value: value),
      ),
    );

Future<void> _rightClickRow(WidgetTester tester, {required int ackMenuId}) async {
  await tester.tap(find.text('row 1'), buttons: kSecondaryButton);
  await tester.pump();
  await tester.pump();
  // The row withholds its menu until Java's MenuDetect verdict allows it.
  await ackMenuDetect(tester, 'Table', 1);
  await ackMenuShown(tester, ackMenuId);
}

Future<void> _pickInsert(WidgetTester tester) async {
  await tester.tap(find.text('Insert...'));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('a context menu replaced by a new Menu still opens on the next '
      'right-click', (tester) async {
    final value = _table(_menu(100));
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    await _rightClickRow(tester, ackMenuId: 100);
    expect(find.text('Insert...'), findsOneWidget,
        reason: 'right-click must open the context menu');

    await _pickInsert(tester);
    expect(find.text('Insert...'), findsNothing);

    value.menu = _menu(200);
    await tester.pumpWidget(_wrap(value));
    await tester.pump();

    await _rightClickRow(tester, ackMenuId: 200);
    expect(find.text('Insert...'), findsOneWidget,
        reason: 'the replacement Menu must open on the next right-click');
  });
}
