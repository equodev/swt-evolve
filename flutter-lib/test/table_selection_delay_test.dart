// Table row selection must update in the same frame as the tap, matching
// Tree. Taps and pumps with no simulated elapsed time, like a real click.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

VTableColumn _col(int id, {int? width, int alignment = SWT.LEFT}) =>
    VTableColumn()
      ..id = id
      ..width = width
      ..alignment = alignment
      ..text = 'Col$id';

VTableItem _item(int id, List<String> texts) => VTableItem()
  ..id = id
  ..texts = texts;

VTable _table({
  required List<VTableColumn> columns,
  required List<VTableItem> items,
}) =>
    VTable()
      ..id = 1
      ..style = SWT.NONE
      ..enabled = true
      ..headerVisible = false
      ..linesVisible = false
      ..columns = columns
      ..items = items
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 800
        ..height = 400);

Widget _wrap(VTable value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 800,
        height: 400,
        child: TableSwt<VTable>(value: value),
      ),
    );

void main() {
  testWidgets(
    'table row is selected instantly on tap, with no gesture-arena delay',
    (tester) async {
      final cols = [_col(1, width: 400)];
      final items = [_item(10, ['Row 0']), _item(11, ['Row 1'])];
      final value = _table(columns: cols, items: items);

      await tester.pumpWidget(_wrap(value));
      await tester.pump();

      expect(value.selection, anyOf(isNull, isEmpty));

      await tester.tap(find.text('Row 1'));
      await tester.pump(Duration.zero);

      expect(
        value.selection,
        equals([1]),
        reason: 'Row must be selected in the same frame as the tap, matching Tree.',
      );
    },
  );
}
