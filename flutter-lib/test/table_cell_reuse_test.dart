// A row rebuilt for a change to one of its cells keeps the others as they were, and a cell is built
// again whenever anything it shows changes: its text, and the check box of a CHECK table's first cell.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VTableItem _item(int i, {int seq = 1, String? first, bool checked = false}) => VTableItem()
  ..id = 100 + i
  ..seq = seq
  ..checked = checked
  ..texts = [first ?? 'row $i / 0', 'row $i / 1'];

VTable _table() => VTable()
  ..id = 1
  ..seq = 1
  ..style = SWT.BORDER | SWT.CHECK
  ..headerVisible = true
  ..itemCount = 3
  ..columns = [
    for (var c = 0; c < 2; c++)
      VTableColumn()
        ..id = 2 + c
        ..seq = 1
        ..width = 140
        ..text = 'Column $c'
  ]
  ..items = [for (var i = 0; i < 3; i++) _item(i)]
  ..bounds = (VRectangle()
    ..width = 300
    ..height = 200);

Widget _cellText(WidgetTester tester, String text) => tester.widget(find.text(text));

bool _checked(WidgetTester tester, int row) {
  final boxes = tester
      .widgetList(find.byWidgetPredicate((w) => w.runtimeType.toString() == '_TableCheckboxButtonWrapper'))
      .toList();
  return (boxes[row] as dynamic).checked as bool;
}

Future<void> _mount(WidgetTester tester) async {
  freshClient();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 300, height: 200, child: TableSwt<VTable>(value: _table())),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('a new text in one cell leaves the row\'s other cells as they were', (tester) async {
    await _mount(tester);
    final untouched = _cellText(tester, 'row 1 / 1');

    await deliverChange(_item(1, seq: 2, first: 'row 1 / 0 changed'), keys: [WireKey.k_TableItem_texts], base: 1);
    await tester.pumpAndSettle();

    expect(find.text('row 1 / 0 changed'), findsOneWidget);
    expect(find.text('row 1 / 0'), findsNothing);
    expect(_cellText(tester, 'row 1 / 1'), same(untouched));
  });

  testWidgets('a check change is drawn', (tester) async {
    await _mount(tester);
    expect(_checked(tester, 0), isFalse);

    await deliverChange(_item(0, seq: 2, checked: true), keys: [WireKey.k_TableItem_checked], base: 1);
    await tester.pumpAndSettle();

    expect(_checked(tester, 0), isTrue);
    expect(_checked(tester, 1), isFalse);
  });
}
