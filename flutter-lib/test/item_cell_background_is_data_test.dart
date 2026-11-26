// A background set for one cell (TableItem/TreeItem.setBackground(int, Color)) is data the
// application shows, like a colour swatch: it is painted whatever use_swt_colors says, and the cell's
// text stays legible on it. A selected row shows its selection there instead, as Cocoa does. The
// row's own background (setBackground(Color)) stays the theme's.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';

const _swatch = 'Swatch';
const _rowText = 'Row text';

VColor _vColor(int r, int g, int b) => VColor()
  ..alpha = 0xFF
  ..red = r
  ..green = g
  ..blue = b;

const _red = Color(0xFFFF0000);
const _yellow = Color(0xFFFFFF00);
const _green = Color(0xFF00C000);

VTable _table({required VColor cell, VColor? row}) => VTable()
  ..id = 1
  ..seq = 1
  ..style = SWT.BORDER
  ..enabled = true
  ..headerVisible = true
  ..itemCount = 2
  ..columns = [
    for (var c = 0; c < 2; c++)
      VTableColumn()
        ..id = 2 + c
        ..seq = 1
        ..width = 120
        ..text = 'Column $c'
  ]
  ..items = [
    VTableItem()
      ..id = 100
      ..seq = 1
      ..texts = ['Named', _swatch]
      ..backgrounds = [null, cell],
    VTableItem()
      ..id = 101
      ..seq = 1
      ..texts = [_rowText, '']
      ..background = row,
  ]
  ..bounds = (VRectangle()
    ..width = 300
    ..height = 200);

VTree _tree({required VColor cell}) => VTree()
  ..id = 1
  ..style = SWT.V_SCROLL
  ..enabled = true
  ..columns = []
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = _swatch
      ..texts = [_swatch]
      ..backgrounds = [cell],
  ]
  ..bounds = (VRectangle()
    ..width = 300
    ..height = 200);

Future<void> _mount(WidgetTester tester, Widget control, {ThemeMode theme = ThemeMode.light}) async {
  freshClient();
  await tester.pumpWidget(EvolveApp(
    theme: theme,
    contentWidget: SizedBox(width: 300, height: 200, child: control),
  ));
  await tester.pumpAndSettle();
}

/// The fills of the containers the text sits in, innermost first.
List<Color> _fillsAround(WidgetTester tester, String text) => tester
    .widgetList<Container>(find.ancestor(of: find.text(text), matching: find.byType(Container)))
    .map((c) => c.color ?? (c.decoration is BoxDecoration ? (c.decoration as BoxDecoration).color : null))
    .whereType<Color>()
    .toList();

Color _textColor(WidgetTester tester, String text) =>
    tester.widget<Text>(find.text(text)).style!.color!;

void main() {
  setUp(() => setConfigFlags(ConfigFlags()));

  testWidgets('a Table cell paints the background the item set for it', (tester) async {
    await _mount(tester, TableSwt<VTable>(value: _table(cell: _vColor(255, 0, 0))));

    expect(_fillsAround(tester, _swatch), contains(_red));
  });

  testWidgets('a Tree cell paints the background the item set for it', (tester) async {
    await _mount(tester, TreeSwt<VTree>(value: _tree(cell: _vColor(255, 0, 0))));

    expect(_fillsAround(tester, _swatch), contains(_red));
  });

  testWidgets('a selected Table row shows its selection over the cell\'s background', (tester) async {
    await _mount(tester,
        TableSwt<VTable>(value: _table(cell: _vColor(255, 0, 0))..selection = [0]));

    expect(_fillsAround(tester, _swatch), isNot(contains(_red)));
  });

  testWidgets('a selected Tree row shows its selection over the cell\'s background', (tester) async {
    final tree = _tree(cell: _vColor(255, 0, 0));
    await _mount(tester, TreeSwt<VTree>(value: tree..selection = [tree.items!.first]));

    expect(_fillsAround(tester, _swatch), isNot(contains(_red)));
  });

  testWidgets('the row\'s own background stays the theme\'s', (tester) async {
    await _mount(tester,
        TableSwt<VTable>(value: _table(cell: _vColor(255, 0, 0), row: _vColor(0, 0xC0, 0))));

    expect(_fillsAround(tester, _rowText), isNot(contains(_green)));
  });

  testWidgets('a Table cell\'s text stays legible on the background it was given', (tester) async {
    await _mount(tester, TableSwt<VTable>(value: _table(cell: _vColor(255, 255, 0))),
        theme: ThemeMode.dark);

    expect(contrastRatio(_textColor(tester, _swatch), _yellow),
        greaterThanOrEqualTo(kMinTextContrast));
  });

  testWidgets('a Tree cell\'s text stays legible on the background it was given', (tester) async {
    await _mount(tester, TreeSwt<VTree>(value: _tree(cell: _vColor(255, 255, 0))),
        theme: ThemeMode.dark);

    expect(contrastRatio(_textColor(tester, _swatch), _yellow),
        greaterThanOrEqualTo(kMinTextContrast));
  });
}
