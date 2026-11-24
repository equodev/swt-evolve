// A multi-column tree whose application sizes its columns for the platform font (a feed view: a
// 100px name column, 80px date/time/author, 50px counters) shows each header label whole, as the
// native control does at those widths.
//
// The tree column's header spans its whole column, as on every native toolkit: only its rows indent
// for the expander. So its title sits where any other column's does, and a narrow tree column shows
// its title as whole as a narrow column elsewhere.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treecolumn.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/treecolumn_evolve.dart';
import 'package:swtflutter/src/impl/utils/bundled_assets.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/tree_theme_extension.dart';

const double _treeWidth = 600;

const _columns = <(String, int)>[
  ('Project', 100),
  ('Date', 80),
  ('Time', 80),
  ('Author', 80),
  ('Total', 50),
  ('Unread', 50),
];

/// 16x16, opaque grey.
const _png =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGUlEQVR42mMwNjb+TwlmGDVg1IBRA4aLAQDChZgQoP+T9QAAAABJRU5ErkJggg==';

// Widgets keep their state by id: a tree given an image is a tree of its own.
VTree _tree(List<(String, int)> columns, {bool firstImage = false}) => VTree()
  ..id = firstImage ? 2 : 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..headerVisible = true
  ..columns = [
    for (var i = 0; i < columns.length; i++)
      VTreeColumn()
        ..id = (firstImage ? 20 : 10) + i
        ..text = columns[i].$1
        ..width = columns[i].$2
        ..image = i == 0 && firstImage
            ? (VImage()..imageData = (VImageData()..data = base64Decode(_png)))
            : null,
  ]
  ..items = [
    VTreeItem()
      ..id = firstImage ? 200 : 100
      ..text = 'Row'
      ..texts = [for (final (label, _) in columns) label],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _treeWidth.toInt()
    ..height = 200);

Future<void> _pump(WidgetTester tester, [List<(String, int)> columns = _columns,
    bool firstImage = false]) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
          width: _treeWidth,
          height: 200,
          child: TreeSwt<VTree>(
              key: ValueKey('$columns $firstImage'),
              value: _tree(columns, firstImage: firstImage))),
    ),
  ));
  // The header decodes its image off the frame: let that finish, then lay out what it produced.
  for (var i = 0; firstImage && i < 5; i++) {
    await tester.runAsync(() => Future<void>.delayed(const Duration(milliseconds: 50)));
    await tester.pump();
  }
  await tester.pumpAndSettle();
}

/// The left edge of the tree column, where its depth-0 row cell starts.
double _firstColumnLeft(WidgetTester tester) =>
    tester.getTopLeft(find.byKey(const ValueKey('tree_cell_100_0'))).dx;

Finder _headerCell(int columnId) =>
    find.byWidgetPredicate((w) => w is TreeColumnSwt && w.value.id == columnId);

Finder _title(int columnId, String shown) =>
    find.descendant(of: _headerCell(columnId), matching: find.text(shown));

/// How much room the header label [shown] has beyond what its glyphs need; negative means it is cut
/// off with an ellipsis.
double _slack(WidgetTester tester, Finder finder) {
  final paragraph = tester.renderObject<RenderBox>(finder);
  return tester.getSize(finder).width - paragraph.getMaxIntrinsicWidth(double.infinity);
}

void main() {
  // The header is measured in the theme's own font, not the test font.
  setUpAll(() async {
    final inter = FontLoader(BundledAssets.family('Inter'))
      ..addFont(rootBundle.load('assets/fonts/Inter_18pt-SemiBold.ttf'));
    await inter.load();
  });

  for (final (label, width) in _columns) {
    testWidgets('a $width px column shows its header "$label" whole', (tester) async {
      await _pump(tester);
      expect(_slack(tester, find.text(label.toUpperCase())), greaterThanOrEqualTo(-0.5));
    });
  }

  testWidgets('a 50 px tree column shows its header whole', (tester) async {
    await _pump(tester, const [('Total', 50), ('Project', 100), ('Unread', 50)]);
    expect(_slack(tester, _title(10, 'TOTAL')), greaterThanOrEqualTo(-0.5));
  });

  testWidgets('the tree column\'s header spans its column and places its title as the others do',
      (tester) async {
    await _pump(tester, const [('Name', 150), ('Date', 150)]);
    expect(tester.getTopLeft(_headerCell(10)).dx - _firstColumnLeft(tester), closeTo(0, 0.5),
        reason: 'natively the first header starts at the column\'s left edge, over the expander');
    final firstInset =
        tester.getTopLeft(_title(10, 'NAME')).dx - tester.getTopLeft(_headerCell(10)).dx;
    final otherInset =
        tester.getTopLeft(_title(11, 'DATE')).dx - tester.getTopLeft(_headerCell(11)).dx;
    expect(firstInset, closeTo(otherInset, 0.5));
    final theme =
        Theme.of(tester.element(find.byType(TreeSwt<VTree>))).extension<TreeThemeExtension>()!;
    expect(firstInset, lessThan(theme.expandIconSize + theme.expandIconSpacing),
        reason: 'natively the title is not pushed past the expander, where the row text starts');
  });

  testWidgets('a narrow header with an image gives up its padding to the image and title',
      (tester) async {
    await _pump(tester, const [('Total', 200), ('Date', 80)]);
    final titleWidth =
        tester.renderObject<RenderBox>(_title(10, 'TOTAL')).getMaxIntrinsicWidth(double.infinity);
    // Room for the image, its gap and the title, and a little padding: less than the theme's.
    final width =
        (titleWidth + TreeColumnImpl.headerImageWidth + TreeColumnImpl.headerImageGap).ceil() + 4;
    await _pump(tester, [('Total', width), ('Date', 80)], true);
    expect(find.descendant(of: _headerCell(20), matching: find.byType(FutureBuilder<Widget?>)),
        findsOneWidget);
    expect(_slack(tester, _title(20, 'TOTAL')), greaterThanOrEqualTo(-0.5));
  });
}
