// A table column's header shows what a native header shows: the text without its mnemonic marks.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';

VTable _table(VTableColumn column) => VTable()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..headerVisible = true
  ..columns = [column]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

Future<void> _pump(WidgetTester tester, String text) async {
  final column = VTableColumn()
    ..id = 2
    ..text = text
    ..width = 120;
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 400, height: 200, child: TableSwt<VTable>(value: _table(column))),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('a header shows its text without the mnemonic marks', (tester) async {
    await _pump(tester, '&Name && kind');

    expect(find.text('Name & kind'), findsOneWidget);
    expect(find.textContaining('&N'), findsNothing);
  });

  testWidgets('a & that ends the header text is drawn', (tester) async {
    await _pump(tester, 'R&D &');

    expect(find.text('RD &'), findsOneWidget);
  });
}
