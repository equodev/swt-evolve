// A Table or Tree column with TableColumn/TreeColumn.setToolTipText shows it while the pointer rests on
// the column's header, as a native header does.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';

VRectangle _bounds() => VRectangle()
  ..x = 0
  ..y = 0
  ..width = 300
  ..height = 120;

VTable _table() => VTable()
  ..swt = 'Table'
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..headerVisible = true
  ..columns = [
    VTableColumn()
      ..id = 2
      ..width = 150
      ..text = 'Name'
      ..toolTipText = 'The name of the entry',
    VTableColumn()
      ..id = 3
      ..width = 150
      ..text = 'Size',
  ]
  ..items = [VTableItem()
    ..id = 10
    ..texts = ['a', 'b']]
  ..bounds = _bounds();

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..headerVisible = true
  ..columns = [
    VTreeColumn()
      ..id = 2
      ..width = 150
      ..alignment = SWT.LEFT
      ..text = 'Name'
      ..toolTipText = 'The name of the entry',
    VTreeColumn()
      ..id = 3
      ..width = 150
      ..alignment = SWT.LEFT
      ..text = 'Size',
  ]
  ..items = [VTreeItem()
    ..id = 10
    ..texts = ['a', 'b']]
  ..bounds = _bounds();

Future<void> _pump(WidgetTester tester, Widget control) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 300, height: 120, child: control),
    ),
  ));
  await tester.pumpAndSettle();
}

/// A header's label, whichever case the theme draws it in.
Finder _header(String text) => find.byWidgetPredicate(
    (w) => w is Text && w.data?.toLowerCase() == text.toLowerCase());

Future<void> _hover(WidgetTester tester, Finder target) async {
  final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await mouse.addPointer(location: Offset.zero);
  addTearDown(mouse.removePointer);
  await mouse.moveTo(tester.getCenter(target));
  await tester.pump(const Duration(seconds: 2));
}

void main() {
  for (final (name, control) in [
    ('Table', () => TableSwt<VTable>(value: _table()) as Widget),
    ('Tree', () => TreeSwt<VTree>(value: _tree()) as Widget),
  ]) {
    testWidgets('a $name column header shows its tooltip under the pointer', (tester) async {
      await _pump(tester, control());

      await _hover(tester, _header('Name'));

      expect(find.text('The name of the entry'), findsOneWidget);
    });

    testWidgets('a $name column header without a tooltip shows none', (tester) async {
      await _pump(tester, control());

      await _hover(tester, _header('Size'));

      expect(find.text('The name of the entry'), findsNothing);
    });
  }
}
