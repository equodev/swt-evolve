// An owner-drawn row's overlay sits under the row, so the row's own text stays above the
// SWT.EraseItem background the overlay replays. The row's background (hover, selection, alternate
// row band) must then sit under the overlay too: laid with the row, it covers everything the
// overlay draws, including the text of every cell the overlay owns.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/impl/owner_draw_overlay.dart';

bool _paints(Color? color) => color != null && color.a > 0;

/// The opaque fills laid over [overlay] by the Stack that holds it. For a Table, only the row the
/// overlay belongs to counts.
List<Color> _fillsOver(WidgetTester tester, Finder overlay, {int? tableRow}) {
  final stack = tester.widget<Stack>(
    find.ancestor(of: overlay, matching: find.byType(Stack)).first,
  );
  final overlayIndex = stack.children.indexWhere(
    (child) =>
        find.descendant(of: find.byWidget(child), matching: overlay).evaluate().isNotEmpty,
  );
  expect(overlayIndex, isNonNegative);

  final fills = <Color>[];
  for (final child in stack.children.skip(overlayIndex + 1)) {
    final layer = find.byWidget(child);
    for (final box in tester.widgetList<DecoratedBox>(
        find.descendant(of: layer, matching: find.byType(DecoratedBox), matchRoot: true))) {
      final decoration = box.decoration;
      if (box.position == DecorationPosition.background &&
          decoration is BoxDecoration &&
          _paints(decoration.color)) {
        fills.add(decoration.color!);
      }
    }
    for (final box in tester.widgetList<ColoredBox>(
        find.descendant(of: layer, matching: find.byType(ColoredBox), matchRoot: true))) {
      if (_paints(box.color)) fills.add(box.color);
    }
    if (tableRow != null) {
      for (final table in tester.widgetList<Table>(
          find.descendant(of: layer, matching: find.byType(Table), matchRoot: true))) {
        final decoration = table.children[tableRow].decoration;
        if (decoration is BoxDecoration && _paints(decoration.color)) {
          fills.add(decoration.color!);
        }
      }
    }
  }
  return fills;
}

VTree _tree() => VTree()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..items = [
    VTreeItem()
      ..id = 10
      ..text = 'owner-drawn'
      ..paintedTexts = [0],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 200);

Future<void> _pumpTree(WidgetTester tester, VTree value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 400, height: 200, child: TreeSwt<VTree>(value: value)),
  ));
  await tester.pumpAndSettle();
}

VTable _table() => VTable()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..headerVisible = false
  ..linesVisible = false
  ..columns = []
  ..items = [
    VTableItem()
      ..id = 10
      ..texts = ['stable'],
    VTableItem()
      ..id = 11
      ..texts = ['owner-drawn']
      ..paintedTexts = [0],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 300);

Future<void> _pumpTable(WidgetTester tester, VTable value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 200, height: 300, child: TableSwt<VTable>(value: value)),
  ));
  await tester.pumpAndSettle();
}

void main() {
  final overlay = find.byType(OwnerDrawnRowOverlay);

  testWidgets('a hovered owner-drawn tree item lays no fill over its overlay', (tester) async {
    await _pumpTree(tester, _tree());

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: Offset.zero);
    addTearDown(mouse.removePointer);
    await mouse.moveTo(tester.getCenter(find.text('owner-drawn')));
    await tester.pumpAndSettle();

    expect(_fillsOver(tester, overlay), isEmpty,
        reason: 'the hover background covers the text the overlay paints');
  });

  testWidgets('a selected owner-drawn tree item lays no fill over its overlay', (tester) async {
    final value = _tree();
    value.selection = [value.items!.first];
    await _pumpTree(tester, value);

    expect(_fillsOver(tester, overlay), isEmpty,
        reason: 'the selection background covers the text the overlay paints');
  });

  testWidgets('a selected owner-drawn table row lays no fill over its overlay', (tester) async {
    final value = _table();
    value.selection = [1];
    await _pumpTable(tester, value);

    expect(_fillsOver(tester, overlay, tableRow: 1), isEmpty,
        reason: 'the selection background covers the text the overlay paints');
  });
}
