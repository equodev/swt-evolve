import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/impl/table_evolve.dart';
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

const _long = 'Integration builds';

/// One column the application sized, with a row per text; widgets are numbered from [id].
VTable _pinned(double width, List<String> texts, {int id = 100}) => VTable()
  ..id = id
  ..style = SWT.NONE
  ..headerVisible = true
  ..columns = [
    VTableColumn()
      ..id = id + 1
      ..width = width.round()
      ..alignment = SWT.LEFT
      ..text = 'Site',
  ]
  ..items = [
    for (var i = 0; i < texts.length; i++)
      VTableItem()
        ..id = id + 10 + i
        ..texts = [texts[i]],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _tableWidth.toInt()
    ..height = 300);

/// A column width [_long] fits only once the padding yields: its text width plus 6.
Future<double> _widthFittingLongOnlyWithLessPadding(WidgetTester tester) async {
  await tester.pumpWidget(_wrap(_pinned(_tableWidth - 20, [_long], id: 200)));
  await tester.pumpAndSettle();
  final textWidth = tester
      .renderObject<RenderBox>(find.text(_long))
      .getMaxIntrinsicWidth(double.infinity);
  await tester.pumpWidget(const SizedBox());
  await tester.pumpAndSettle();
  return textWidth + 6;
}

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

  testWidgets('a row added after the first build starts where the rows already shown start',
      (tester) async {
    final table = _pinned(await _widthFittingLongOnlyWithLessPadding(tester), ['Releases']);
    await tester.pumpWidget(_wrap(table));
    await tester.pumpAndSettle();

    table.items = [...table.items!, VTableItem()..id = 150..texts = [_long]];
    // ignore: invalid_use_of_protected_member
    tester.state(find.byType(TableSwt<VTable>)).setState(() {});
    await tester.pumpAndSettle();

    expect(tester.getTopLeft(find.text(_long)).dx,
        tester.getTopLeft(find.text('Releases')).dx);
  });

  testWidgets('a row whose text changes starts where the other rows start', (tester) async {
    final table =
        _pinned(await _widthFittingLongOnlyWithLessPadding(tester), ['Releases', 'Updates']);
    await tester.pumpWidget(_wrap(table));
    await tester.pumpAndSettle();

    table.items![1].texts = [_long];
    final impl = tester.state<TableImpl>(find.byType(TableSwt<VTable>));
    // ignore: invalid_use_of_protected_member
    impl.setValue(table);
    await tester.pumpAndSettle();

    expect(tester.getTopLeft(find.text(_long)).dx,
        tester.getTopLeft(find.text('Releases')).dx);
  });
}
