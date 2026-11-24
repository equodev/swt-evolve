import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

/// A check table whose columns the application sized: every row starts its
/// texts at the same x in each column, whether the text fits the column or is
/// cut off.
const double _tableWidth = 600;

const _rows = [
  ['2026-06', 'https://download.eclipse.org/releases/2026-06', 'Enabled'],
  [
    'Eclipse IDE integration for Maven and other long names',
    'https://download.eclipse.org/technology/m2e/releases/latest/and/more',
    'Enabled'
  ],
  ['SWT Evolve', 'https://dl.equo.dev/swt-evolve/p2', 'Enabled'],
];

VTable _table() => VTable()
  ..id = 1
  ..style = SWT.CHECK
  ..headerVisible = true
  ..linesVisible = true
  ..columns = [
    VTableColumn()
      ..id = 2
      ..width = 180
      ..alignment = SWT.LEFT
      ..text = 'Name',
    VTableColumn()
      ..id = 3
      ..width = 260
      ..alignment = SWT.LEFT
      ..text = 'Location',
    VTableColumn()
      ..id = 4
      ..width = 100
      ..alignment = SWT.LEFT
      ..text = 'Enabled',
  ]
  ..items = [
    for (var i = 0; i < _rows.length; i++)
      VTableItem()
        ..id = 10 + i
        ..texts = _rows[i],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _tableWidth.toInt()
    ..height = 300);

Widget _wrap(VTable value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: _tableWidth,
        height: 300,
        child: TableSwt<VTable>(value: value),
      ),
    );

void main() {
  testWidgets('every row starts its Name text at the same x', (tester) async {
    await tester.pumpWidget(_wrap(_table()));
    await tester.pumpAndSettle();

    final xs = [for (final r in _rows) tester.getTopLeft(find.text(r[0])).dx];
    expect(xs.toSet(), hasLength(1), reason: 'Name x per row: $xs');
  });

  testWidgets('every row starts its Location text at the same x',
      (tester) async {
    await tester.pumpWidget(_wrap(_table()));
    await tester.pumpAndSettle();

    final xs = [for (final r in _rows) tester.getTopLeft(find.text(r[1])).dx];
    expect(xs.toSet(), hasLength(1), reason: 'Location x per row: $xs');
  });

  testWidgets('a cut-off Name keeps the gap before Location', (tester) async {
    await tester.pumpWidget(_wrap(_table()));
    await tester.pumpAndSettle();

    final longRow = _rows[1];
    final nameRight = tester.getTopRight(find.text(longRow[0])).dx;
    final locationLeft = tester.getTopLeft(find.text(longRow[1])).dx;
    expect(locationLeft - nameRight, greaterThan(0),
        reason: 'Name ends at $nameRight, Location starts at $locationLeft');
  });
}
