// A Table only reserves trim when SWT.BORDER asks for it. Painted unconditionally, the frame eats
// into the rows, and an application that sizes the table as rows * getItemHeight() - NatTable's
// NatCombo popup does - loses its last row to the border and gets a few pixels of scroll range.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';

const _rows = 4;
const _height = 300.0;

VTable _table({required int style}) => VTable()
  ..id = 1
  ..style = style
  ..headerVisible = false
  ..linesVisible = false
  ..columns = [
    VTableColumn()
      ..id = 1
      ..width = 200
      ..alignment = SWT.LEFT
      ..text = 'Col1'
  ]
  ..items = List.generate(
    _rows,
    (i) => VTableItem()
      ..id = 10 + i
      ..texts = ['Item $i'],
  )
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = _height.toInt());

Widget _wrap(VTable value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 200,
        height: _height,
        child: TableSwt<VTable>(value: value),
      ),
    );

/// The frame the table draws around its own content, or null when it draws none.
BoxBorder? _frameBorder(WidgetTester tester) {
  for (final container in tester.widgetList<Container>(find.byType(Container))) {
    final decoration = container.decoration;
    if (decoration is BoxDecoration && decoration.border != null) {
      return decoration.border;
    }
  }
  return null;
}

void main() {
  testWidgets('a borderless Table draws no frame', (tester) async {
    await tester.pumpWidget(_wrap(_table(style: SWT.NONE)));

    expect(_frameBorder(tester), isNull);
  });

  testWidgets('a borderless Table starts its rows at its own top edge', (tester) async {
    await tester.pumpWidget(_wrap(_table(style: SWT.NONE)));

    final tableTop = tester.getTopLeft(find.byType(TableSwt<VTable>)).dy;
    final bodyTop = tester.getTopLeft(find.byType(Table).last).dy;

    expect(bodyTop, tableTop);
  });

  testWidgets('SWT.BORDER still draws the frame', (tester) async {
    await tester.pumpWidget(_wrap(_table(style: SWT.BORDER)));

    expect(_frameBorder(tester), isNotNull);
  });
}
